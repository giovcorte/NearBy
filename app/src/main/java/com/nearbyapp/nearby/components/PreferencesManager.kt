package com.nearbyapp.nearby.components

import android.content.SharedPreferences
import com.nearbyapp.nearby.AppConstants
import com.nearbyapp.nearby.model.settings.CachePreference
import com.nearbyapp.nearby.model.settings.RadiusPreference
import com.nearbyapp.nearby.model.settings.TravelMode
import com.nearbyapp.nearby.model.settings.TravelModePreference

class PreferencesManager(private val sharedPreferences: SharedPreferences) {

    companion object {
        const val RADIUS = "radius"
        const val TRAVEL_MODE = "travel_model"
        const val CACHE_FALLBACK_ENABLED = "cache-fallback-enabled"
    }

    fun getRadius(): RadiusPreference {
        val radius = sharedPreferences.getInt(RADIUS, 5)
        return RadiusPreference(2, radius, 50)
    }

    fun putRadius(radius: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(RADIUS, radius)
        editor.apply()
    }

    fun putCacheEnabled(boolean: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(CACHE_FALLBACK_ENABLED, boolean)
        editor.apply()
    }

    fun getCacheEnabled() : CachePreference {
        return CachePreference("In assenza di connessione, mostrare direttamente le ricerche recenti se presenti nella cache",
            sharedPreferences.getBoolean(CACHE_FALLBACK_ENABLED, false))
    }

    fun getTravelMode(): TravelModePreference {
        return TravelModePreference(
            TravelMode(AppConstants.WALKING, isCurrentTravelMode(AppConstants.WALKING), "Percorsi percorribili a piedi"),
            TravelMode(AppConstants.DRIVING, isCurrentTravelMode(AppConstants.DRIVING), "Percorsi percorribili in automobile")
        )
    }

    fun putTravelMode(travelMode: String) {
        val editor = sharedPreferences.edit()
        editor.putString(TRAVEL_MODE, travelMode)
        editor.apply()
    }

    fun getTravelModeString(): String {
        val result = sharedPreferences.getString(TRAVEL_MODE, AppConstants.WALKING)
        result?.let { return result }
        return AppConstants.WALKING
    }

    private fun isCurrentTravelMode(travelMode: String): Boolean {
        val currentTravelMode = sharedPreferences.getString(TRAVEL_MODE, AppConstants.WALKING)
        return travelMode == currentTravelMode
    }

}