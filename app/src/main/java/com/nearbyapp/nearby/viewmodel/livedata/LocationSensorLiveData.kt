package com.nearbyapp.nearby.viewmodel.livedata

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.location.LocationManager
import androidx.lifecycle.MutableLiveData
import com.nearbyapp.nearby.components.GPSReceiver

class LocationSensorLiveData(val application: Application): MutableLiveData<Boolean>() {

    private var locationManager: LocationManager = (application.getSystemService(Context.LOCATION_SERVICE) as LocationManager)

    private val gpsReceiver = GPSReceiver(object : GPSReceiver.LocationSensorCallBack {
        override fun enabled() {
            postValue(true)
        }

        override fun disabled() {
            postValue(false)
        }

    })

    init {
        postValue(isLocationEnabled())
    }

    override fun onActive() {
        super.onActive()
        application.registerReceiver(gpsReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        application.registerReceiver(gpsReceiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
    }

    override fun onInactive() {
        application.unregisterReceiver(gpsReceiver)
        super.onInactive()
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun isActive(): Boolean {
        value?.let {
            return it
        }
        return true
    }

}