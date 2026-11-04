package com.ejemplo.caminarcorrer.phone

import android.app.Application

class PhoneApplication : Application() {

    lateinit var container: PhoneAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = PhoneAppContainer(this)
    }
}
