package com.nearbyapp.nearby.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nearbyapp.nearby.components.ResponseWrapper
import com.nearbyapp.nearby.model.TextWrapper
import com.nearbyapp.nearby.recycler.Identifiable
import com.nearbyapp.nearby.repository.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NearByViewModel(application: Application): BaseViewModel(application) {

    private val places: MutableList<Identifiable> = mutableListOf()
    val placesData: MutableLiveData<MutableList<Identifiable>> = MutableLiveData()
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
                        places.addAll(response.value?.results?.map{
                            it.apply {
                                userLat = latitude
                                userLng = longitude
                            }
                        } ?: run {
                            listOf(TextWrapper("Nessun luogo trovato"))
                        })
                        placesData.postValue(places)
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
                            val newPlaces = response.value?.results?.map{
                                it.userLat = userLatitude
                                it.userLng = userLongitude
                                it
                            }?.toMutableList()
                            newPlaces?.let {
                                places.addAll(it)
                                placesData.postValue(places)
                            } ?: run {
                                placesData.postValue(places)
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