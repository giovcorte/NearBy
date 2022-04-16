package com.nearbyapp.nearby.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager

class GPSReceiver(private val locationSensorCallBack: LocationSensorCallBack) : BroadcastReceiver() {

    interface LocationSensorCallBack {
        fun enabled()
        fun disabled()
    }

    override fun onReceive(context: Context, intent: Intent) {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (isGpsEnabled || isNetworkEnabled) {
            locationSensorCallBack.enabled()
        } else {
            locationSensorCallBack.disabled()
        }
    }

}