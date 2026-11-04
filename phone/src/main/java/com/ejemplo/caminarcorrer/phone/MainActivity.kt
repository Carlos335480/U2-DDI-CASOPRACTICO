package com.ejemplo.caminarcorrer.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ejemplo.caminarcorrer.phone.ui.PhoneApp
import com.ejemplo.caminarcorrer.phone.ui.theme.CaminarCorrerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as PhoneApplication).container
        setContent {
            CaminarCorrerTheme {
                PhoneApp(container)
            }
        }
    }
}
