package com.example.andrapp.maps.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.andrapp.legacy.User
import com.example.andrapp.maps.domain.model.Pin
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import com.example.andrapp.R
import com.example.andrapp.util.Resource

@Composable
fun MapScreen(
    user: User?,
    mapViewModel: MapViewModel
) {
    val pinsState by mapViewModel.pinsState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()

    var isMapLoaded by remember { mutableStateOf(false) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showAddPinDialog by remember { mutableStateOf<LatLng?>(null) }
    var showPinInfoDialog by remember { mutableStateOf<Pin?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                hasLocationPermission = true
            } else {
                Toast.makeText(context, context.getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(hasLocationPermission, isMapLoaded) {
        if (hasLocationPermission && isMapLoaded) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            update = com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
                                CameraPosition(userLatLng, 15f, 0f, 0f)
                            ),
                            durationMs = 1000
                        )
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = pinsState) {
            is Resource.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is Resource.Error -> {
                Text(
                    text = stringResource(R.string.error_prefix, state.errorMessage),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is Resource.Success -> {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                    onMapLongClick = { latLng ->
                        showAddPinDialog = latLng
                    },
                    onMapLoaded = {
                        isMapLoaded = true
                    }
                ) {
                    state.data.forEach { pin ->
                        Marker(
                            state = MarkerState(position = LatLng(pin.latitude, pin.longitude)),
                            title = pin.name,
                            snippet = pin.description,
                            onInfoWindowClick = {
                                showPinInfoDialog = pin
                            }
                        )
                    }
                }

                user?.let {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.map_for_user, user.username),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                if (!hasLocationPermission) {
                    FloatingActionButton(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = stringResource(R.string.enable_location))
                    }
                }
            }

        }

    }



    if (showAddPinDialog != null) {
        AddPinDialog(
            latLng = showAddPinDialog!!,
            onDismiss = { showAddPinDialog = null },
            onSave = { name, description ->
                mapViewModel.addPin(name, description, showAddPinDialog!!.latitude, showAddPinDialog!!.longitude)
                showAddPinDialog = null
            }
        )
    }

    if (showPinInfoDialog != null) {
        PinInfoDialog(
            pin = showPinInfoDialog!!,
            onDismiss = { showPinInfoDialog = null }
        )
    }
}

@Composable
private fun AddPinDialog(
    latLng: LatLng,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.save_new_pin))},
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.pin_name_hint)) }
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.pin_description_hint)) },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, description) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun PinInfoDialog(pin: Pin, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(pin.name) },
        text = { pin.description?.let { Text(it) } },
        confirmButton = {
            Button(onClick = onDismiss) { Text(stringResource(R.string.ok)) }
        }
    )
}