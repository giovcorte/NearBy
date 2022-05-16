package com.nearbyapp.nearby.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nearbyapp.nearby.model.settings.CachePreference
import com.nearbyapp.nearby.model.settings.RadiusPreference
import com.nearbyapp.nearby.model.settings.TravelModePreference

class SettingsViewModel(application: Application): BaseViewModel(application) {

    var radiusPreference: MutableLiveData<RadiusPreference> = MutableLiveData()
    var travelModePreference: MutableLiveData<TravelModePreference> = MutableLiveData()
    var cachedPref: MutableLiveData<CachePreference> = MutableLiveData()

    fun loadPreferences() {
        radiusPreference.postValue(preferencesManager.getRadius())
        travelModePreference.postValue(preferencesManager.getTravelMode())
        cachedPref.postValue(preferencesManager.getCacheEnabled())
    }
}