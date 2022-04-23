package com.nearbyapp.nearby.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.databinding.databinding.IData
import com.nearbyapp.nearby.model.NearbyPlaceWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedPlacesViewModel(application: Application): BaseViewModel(application) {

    val places: MutableLiveData<List<IData>> = MutableLiveData()

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