package com.nearbyapp.nearby.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.databinding.databinding.IData
import com.google.android.gms.maps.model.PolylineOptions
import com.nearbyapp.nearby.components.ResponseWrapper
import com.nearbyapp.nearby.model.TextWrapper
import com.nearbyapp.nearby.model.detail.Detail
import com.nearbyapp.nearby.repository.DataSource
import com.nearbyapp.nearby.viewmodel.livedata.ImagesLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailViewModel(application: Application): AbstractDetailViewModel(application) {

    private var job: Job? = null

    var lat: Double = 0.0
    var lng: Double = 0.0
    var detail: Detail? = null

    val favorite: MutableLiveData<Boolean> = MutableLiveData()
    val imagesState: ImagesLiveData = ImagesLiveData(application)

    override fun loadDetails(id: String, dataSource: DataSource) {
        loading.postValue(true)
        job = viewModelScope.launch {
            when (val response = repository.getPlaceDetail(id, dataSource)) {
                is ResponseWrapper.Success -> {
                    detail = response.value?.detail
                    val placeLat = response.value?.detail?.geometry?.location?.lat
                    val placeLng = response.value?.detail?.geometry?.location?.lng
                    var polyline: PolylineOptions? = null
                    if (dataSource != DataSource.CACHE) {
                        polyline = getPolyline(repository.getPolyline(lat, lng, placeLat!!, placeLng!!))
                    }
                    val fav = repository.existPlace(id)
                    withContext(Dispatchers.Main) {
                        val details: MutableList<IData?> = mutableListOf()
                        response.value?.detail?.let {
                            details.add(getImages(it.photos))
                            details.add(it)
                            if (dataSource != DataSource.CACHE) {
                                details.add(getMap(it, lat, lng, polyline))
                            }
                            details.add(getReviews(it))
                            details.add(it.opening_hours)
                        } ?: run {
                            details.add(TextWrapper("Nessun dettaglio disponibile"))
                        }
                        this@DetailViewModel.favorite.postValue(fav)
                        this@DetailViewModel.details.postValue(details.filterNotNull().toMutableList())
                        this@DetailViewModel.loading.postValue(false)
                    }
                }
                is ResponseWrapper.Error -> { loading.postValue(false) }
            }
        }
    }

    fun saveDetails() {
        detail?.let { detail ->
            job = viewModelScope.launch {
                if (!repository.existPlace(detail.place_id)) {
                    detail.photos?.mapIndexed { i, photo ->
                        if (imageLoader.cache().contains(photo.link)) {
                            photo.id = imageStorageHelper.getImageId(detail.place_id, i)
                        }
                    }
                    repository.savePlaceDetail(detail)
                }
            }
            favorite.postValue(true)
        }
    }

    fun deletePlace() {
        super.deleteDetails(detail!!)
        favorite.postValue(false)
    }

    fun saveImage() {
        viewModelScope.launch {
            detail?.photos?.let {
                imageStorageHelper.storeImages(detail!!.place_id, it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}