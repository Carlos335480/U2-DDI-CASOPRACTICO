package com.ejemplo.caminarcorrer.wear.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.wearDataStore by preferencesDataStore(name = "wear_data")

object WearKeys {
    val PROFILE = stringPreferencesKey("profile_json")
    val ACTIVITIES = stringPreferencesKey("activities_json")
}
