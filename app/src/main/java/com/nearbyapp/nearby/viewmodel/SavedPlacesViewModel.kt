package com.nearbyapp.nearby.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nearbyapp.nearby.model.NearbyPlaceWrapper
import com.nearbyapp.nearby.recycler.Identifiable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedPlacesViewModel(application: Application): BaseViewModel(application) {

    val places: MutableLiveData<List<Identifiable>> = MutableLiveData()

    fun loadSavedPlaces() {
        viewModelScope.launch {
            val storedPlaces = repository.getStoredPlaces()
            withContext(Dispatchers.Main) {
                places.postValue(storedPlaces.map { NearbyPlaceWrapper(it) })
            }
        }
    }

    fun deletePlace(id: String) {
        viewModelScope.launch {
            listOf(10).forEach {
                imageStorageHelper.deleteStoredImage("$id-$it")
            }
            repository.deletePlaceDetail(id)
        }
    }
}