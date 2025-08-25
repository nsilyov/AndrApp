package com.example.andrapp

import android.app.Application
import com.example.andrapp.di.DependencyContainer

class AndrApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DependencyContainer.init(this)
    }
}