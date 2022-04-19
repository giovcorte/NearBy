package com.nearbyapp.nearby.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import com.nearbyapp.nearby.components.Status
import com.nearbyapp.nearby.viewmodel.livedata.ConnectivityLiveData
import com.nearbyapp.nearby.viewmodel.livedata.LocationSensorLiveData
import com.nearbyapp.nearby.viewmodel.livedata.ServiceLiveData

class ActivityViewModel(application: Application): AndroidViewModel(application) {

    private val connectivityState: ConnectivityLiveData = ConnectivityLiveData(application)
    private val locationState: LocationSensorLiveData = LocationSensorLiveData(application)
    private val serviceState: ServiceLiveData = ServiceLiveData(application)

    val errorState: MediatorLiveData<Status> = MediatorLiveData()

    init {
        errorState.postValue(isReady())
        errorState.addSource(connectivityState) { active ->
            if (!active) {
                errorState.postValue(Status.INTERNET)
            } else if (locationState.isActive() && serviceState.isActive()) {
                errorState.postValue(Status.READY)
            }
        }
        errorState.addSource(locationState) { active ->
            if (!active) {
                errorState.postValue(Status.LOCATION)
            } else if (connectivityState.isActive() && serviceState.isActive()) {
                errorState.postValue(Status.READY)
            }
        }
        errorState.addSource(serviceState) { active ->
            if (!active) {
                errorState.postValue(Status.SERVICE)
            } else if (connectivityState.isActive() && locationState.isActive()) {
                errorState.postValue(Status.READY)
            }
        }
    }

    fun postServiceValue(active: Boolean) {
        serviceState.postValue(active)
    }

    private fun isReady(): Status {
        var status: Status = Status.READY
        if (!connectivityState.isActive()) {
            status = Status.INTERNET
        }
        if (!locationState.isActive()) {
            status = Status.LOCATION
        }
        if (!serviceState.isActive()) {
            status = Status.SERVICE
        }
        return status
    }

}