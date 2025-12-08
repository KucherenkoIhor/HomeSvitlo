package com.home.svitlo.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "inverter_status")

data class StoredInverterStatus(
    val statusCode: String,
    val batteryCharge: Double,
    val lastUpdated: Long
)

class InverterStatusStorage(private val context: Context) {

    private object Keys {
        val STATUS_CODE = stringPreferencesKey("status_code")
        val BATTERY_CHARGE = doublePreferencesKey("battery_charge")
        val LAST_UPDATED = longPreferencesKey("last_updated")
    }

    val statusFlow: Flow<StoredInverterStatus?> = context.dataStore.data.map { preferences ->
        val statusCode = preferences[Keys.STATUS_CODE]
        val batteryCharge = preferences[Keys.BATTERY_CHARGE]
        val lastUpdated = preferences[Keys.LAST_UPDATED]
        
        if (statusCode != null && batteryCharge != null && lastUpdated != null) {
            StoredInverterStatus(statusCode, batteryCharge, lastUpdated)
        } else {
            null
        }
    }

    suspend fun getStatus(): StoredInverterStatus? {
        val preferences = context.dataStore.data.first()
        val statusCode = preferences[Keys.STATUS_CODE]
        val batteryCharge = preferences[Keys.BATTERY_CHARGE]
        val lastUpdated = preferences[Keys.LAST_UPDATED]
        
        return if (statusCode != null && batteryCharge != null && lastUpdated != null) {
            StoredInverterStatus(statusCode, batteryCharge, lastUpdated)
        } else {
            null
        }
    }

    suspend fun saveStatus(statusCode: String, batteryCharge: Double) {
        context.dataStore.edit { preferences ->
            preferences[Keys.STATUS_CODE] = statusCode
            preferences[Keys.BATTERY_CHARGE] = batteryCharge
            preferences[Keys.LAST_UPDATED] = System.currentTimeMillis()
        }
    }

    suspend fun getPreviousStatusCode(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[Keys.STATUS_CODE]
    }
}

