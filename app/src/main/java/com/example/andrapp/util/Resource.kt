package com.example.andrapp.util


sealed class Resource<out T : Any> {
    data class Success<out T : Any>(val data: T) : Resource<T>()
    data class Error(val errorMessage: String) : Resource<Nothing>()
    data class Loading<out T : Any>(val data: T? = null) : Resource<T>()
}