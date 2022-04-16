package com.nearbyapp.nearby.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.databinding.databinding.IData
import com.google.android.gms.maps.model.PolylineOptions
import com.nearbyapp.nearby.AppController
import com.nearbyapp.nearby.components.ResponseWrapper
import com.nearbyapp.nearby.converters.PolylineParser.parsePolylineOptions
import com.nearbyapp.nearby.model.ListWrapper
import com.nearbyapp.nearby.model.MapWrapper
import com.nearbyapp.nearby.model.detail.Detail
import com.nearbyapp.nearby.repository.RepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DetailViewModel(application: Application): BaseViewModel(application) {

    private var job: Job? = null

    val details: MutableLiveData<MutableList<IData>> = MutableLiveData()

    fun loadDetails(id: String, lat: Double, lng: Double) {
        loading.postValue(true)
        job = viewModelScope.launch {
            when (val response = repository.getPlaceDetail(id)) {
                is ResponseWrapper.Success -> {
                    val placeLat = response.value?.detail?.geometry?.location?.lat
                    val placeLng = response.value?.detail?.geometry?.location?.lng
                    val polyline = getPolyline(repository.getPolyline(placeLat!!, placeLng!!, lat, lng))
                    withContext(Dispatchers.Main) {
                        val details: MutableList<IData?> = mutableListOf()
                        response.value.detail.let {
                            details.add(getImages(it))
                            details.add(it)
                            details.add(getMap(it, lat, lng, polyline))
                            details.add(getReviews(it))
                            details.add(it.opening_hours)
                        }
                        this@DetailViewModel.details.postValue(details.filterNotNull().toMutableList())
                        this@DetailViewModel.loading.postValue(false)
                    }
                }
                is ResponseWrapper.Error -> {loading.postValue(false)}
            }
        }
    }

    private fun getPolyline(response: ResponseWrapper<JSONObject?>): PolylineOptions? {
        return when(response) {
            is ResponseWrapper.Success -> {
                parsePolylineOptions(response.value)
            }
            else -> null
        }
    }

    private fun getImages(detail: Detail) : IData? {
        detail.photos?.also {
            return ListWrapper(detail.photos.toMutableList(), true)
        }
        return null
    }

    private fun getReviews(detail: Detail): IData? {
        detail.reviews?.let {
            return ListWrapper(detail.reviews.mapIndexed { i, r ->
                r.page = (i + 1).toString()
                r
            }.toMutableList(), true)
        }
        return null
    }

    private fun getMap(detail: Detail, userLat: Double, userLng: Double, polylineOptions: PolylineOptions?): IData? {
        detail.geometry?.location?.let {
            return MapWrapper(it.lat, it.lng, userLat, userLng, polylineOptions, "walking")
        }
        return null
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}