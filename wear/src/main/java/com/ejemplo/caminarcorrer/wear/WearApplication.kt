package com.ejemplo.caminarcorrer.wear

import android.app.Application

class WearApplication : Application() {

    lateinit var container: WearAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = WearAppContainer(this)
    }
}
