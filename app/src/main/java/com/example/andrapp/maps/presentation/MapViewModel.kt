package com.example.andrapp.maps.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.andrapp.di.DependencyContainer
import com.example.andrapp.legacy.User
import com.example.andrapp.legacy.UserSessionManager
import com.example.andrapp.maps.domain.model.Pin
import com.example.andrapp.maps.domain.usecase.AddPinUseCase
import com.example.andrapp.maps.domain.usecase.GetPinsUseCase
import com.example.andrapp.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val getPinsUseCase: GetPinsUseCase = DependencyContainer.getPinsUseCase
    private val addPinUseCase: AddPinUseCase = DependencyContainer.addPinUseCase

    private val sessionManager = UserSessionManager.getInstance(application)

    private val _currentUser = MutableStateFlow(sessionManager.loggedInUser)
    val currentUser: StateFlow<User?> = _currentUser

    @OptIn(ExperimentalCoroutinesApi::class)
    val pinsState: StateFlow<Resource<List<Pin>>> = currentUser.flatMapLatest { user ->
        if (user != null) {
            getPinsUseCase(user.userId)
        } else {
            flowOf(Resource.Success(emptyList()))
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Loading()
    )

    fun addPin(name: String, description: String?, lat: Double, lng: Double) =
        viewModelScope.launch {
            addPinUseCase(
                Pin(
                    userId = sessionManager.loggedInUser.userId,
                    name = name,
                    description = description,
                    latitude = lat,
                    longitude = lng
                )
            )
        }
}