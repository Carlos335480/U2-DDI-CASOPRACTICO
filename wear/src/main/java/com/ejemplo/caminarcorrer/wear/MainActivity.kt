package com.ejemplo.caminarcorrer.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ejemplo.caminarcorrer.wear.ui.WearApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as WearApplication).container
        setContent {
            WearApp(container)
        }
    }
}
