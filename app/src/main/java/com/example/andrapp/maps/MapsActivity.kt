@file:Suppress("DEPRECATION")

package com.example.andrapp.maps

import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.andrapp.di.DependencyContainer
import com.example.andrapp.legacy.User
import com.example.andrapp.maps.presentation.MapScreen
import com.example.andrapp.maps.presentation.MapViewModel

class MapsActivity : ComponentActivity() {

    private val mapViewModel: MapViewModel by viewModels {
        DependencyContainer.MapViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("USER_EXTRA", User::class.java)
        } else {
            intent.getSerializableExtra("USER_EXTRA") as? User
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapScreen(user, mapViewModel)
                }
            }
        }
    }
}