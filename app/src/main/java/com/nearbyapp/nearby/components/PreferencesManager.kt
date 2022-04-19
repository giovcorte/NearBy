package com.nearbyapp.nearby.components

import android.content.SharedPreferences
import com.nearbyapp.nearby.AppConstants
import com.nearbyapp.nearby.model.RadiusPreference
import com.nearbyapp.nearby.model.TravelMode
import com.nearbyapp.nearby.model.TravelModePreference

class PreferencesManager(private val sharedPreferences: SharedPreferences) {

    companion object {
        const val RADIUS = "radius"
        const val TRAVEL_MODE = "travel_model"
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