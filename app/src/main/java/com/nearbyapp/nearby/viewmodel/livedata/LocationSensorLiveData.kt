package com.nearbyapp.nearby.viewmodel.livedata

import android.app.Application
import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.MutableLiveData

class LocationSensorLiveData(application: Application): MutableLiveData<Boolean>() {

    private var locationManager: LocationManager = (application.getSystemService(Context.LOCATION_SERVICE) as LocationManager)

    init {
        postValue(isLocationEnabled())
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