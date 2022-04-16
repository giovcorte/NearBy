package com.nearbyapp.nearby.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.databinding.databinding.IData
import com.nearbyapp.nearby.components.ResponseWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NearByViewModel(application: Application): BaseViewModel(application) {

    val places: MutableLiveData<MutableList<IData>> = MutableLiveData()
    var position: Int = 0
    var token: String? = null

    var lat: Double = 0.0
    var lng: Double = 0.0

    var job: Job? = null

    fun loadPlaces(lat: Double, lng: Double, query: String) {
        loading.postValue(true)
        job = viewModelScope.launch {
            when (val response = repository.getNearbyPlaces(lat, lng, preferencesManager.getRadius().getRadiusInKm(), query)) {
                is ResponseWrapper.Success -> {
                    withContext(Dispatchers.Main) {
                        token = response.value?.next_page_token
                        places.postValue(response.value?.results?.map{
                            it.userLat = lat
                            it.userLng = lng
                            it
                        }?.toMutableList() )
                        loading.postValue(false)
                    }
                }
                is ResponseWrapper.Error -> loading.postValue(false)
            }
        }
    }

    fun loadMorePlaces() {
        token?.let {
            job = viewModelScope.launch {
                when (val response = repository.getMorePlaces(it)) {
                    is ResponseWrapper.Success -> {
                        withContext(Dispatchers.Main) {
                            token = response.value?.next_page_token
                            val currentPlaces = places.value
                            val newPlaces = response.value?.results?.map{
                                it.userLat = lat
                                it.userLng = lng
                                it
                            }?.toMutableList()!!
                            currentPlaces?.addAll(newPlaces)
                            currentPlaces?.let {
                                places.postValue(currentPlaces.toMutableList())
                            }
                            loading.postValue(false)
                        }
                    }
                    is ResponseWrapper.Error -> loading.postValue(false)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}