package com.nearbyapp.nearby.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nearbyapp.nearby.model.RadiusPreference
import com.nearbyapp.nearby.model.TravelModePreference

class SettingsViewModel(application: Application): BaseViewModel(application) {

    var radiusPreference: MutableLiveData<RadiusPreference> = MutableLiveData()
    var travelModePreference: MutableLiveData<TravelModePreference> = MutableLiveData()

    fun loadPreferences() {
        radiusPreference.postValue(preferencesManager.getRadius())
        travelModePreference.postValue(preferencesManager.getTravelMode())
    }
}