package com.ejemplo.caminarcorrer.phone.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.phoneDataStore by preferencesDataStore(name = "phone_data")

object PhoneKeys {
    val PROFILE = stringPreferencesKey("profile_json")
    val ACTIVITIES = stringPreferencesKey("activities_json")
}
