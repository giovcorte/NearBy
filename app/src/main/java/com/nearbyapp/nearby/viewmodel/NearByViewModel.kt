package com.nearbyapp.nearby.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.databinding.databinding.IData
import com.nearbyapp.nearby.components.ResponseWrapper
import com.nearbyapp.nearby.model.TextWrapper
import com.nearbyapp.nearby.repository.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NearByViewModel(application: Application): BaseViewModel(application) {

    val places: MutableLiveData<MutableList<IData>> = MutableLiveData()
    var position: Int = 0
    var token: String? = null

    var userLatitude: Double = 0.0
    var userLongitude: Double = 0.0

    var job: Job? = null

    fun loadPlaces(latitude: Double, longitude: Double, query: String, dataSource: DataSource) {
        loading.postValue(true)
        job = viewModelScope.launch {
            when (val response = repository.getNearbyPlaces(
                latitude,
                longitude,
                preferencesManager.getRadius().getRadiusInKm(),
                query,
                dataSource
            )) {
                is ResponseWrapper.Success -> {
                    withContext(Dispatchers.Main) {
                        token = response.value?.next_page_token
                        places.postValue(response.value?.results?.map{
                            it.apply {
                                userLat = latitude
                                userLng = longitude
                            }
                        }?.toMutableList() ?: run {
                            mutableListOf(TextWrapper("Nessun luogo trovato"))
                        })
                        loading.postValue(false)
                    }
                }
                is ResponseWrapper.Error -> loading.postValue(false)
            }
        }
    }

    fun loadMorePlaces(dataSource: DataSource) {
        token?.let {
            job = viewModelScope.launch {
                when (val response = repository.getMorePlaces(it, dataSource)) {
                    is ResponseWrapper.Success -> {
                        withContext(Dispatchers.Main) {
                            token = response.value?.next_page_token
                            val currentPlaces = places.value
                            val newPlaces = response.value?.results?.map{
                                it.userLat = userLatitude
                                it.userLng = userLongitude
                                it
                            }?.toMutableList()
                            newPlaces?.let {
                                currentPlaces?.addAll(it)
                                currentPlaces?.let {
                                    places.postValue(currentPlaces.toMutableList())
                                }
                            } ?: run {
                                currentPlaces?.let {
                                    places.postValue(currentPlaces.toMutableList())
                                }
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