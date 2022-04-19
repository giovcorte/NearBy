package com.nearbyapp.nearby.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.databinding.databinding.IData
import com.nearbyapp.nearby.components.ResponseWrapper
import com.nearbyapp.nearby.model.detail.Detail
import com.nearbyapp.nearby.viewmodel.livedata.ImagesLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailViewModel(application: Application): AbstractDetailViewModel(application) {

    private var job: Job? = null
    var detail: Detail? = null

    val details: MutableLiveData<MutableList<IData>> = MutableLiveData()
    val favorite: MutableLiveData<Boolean> = MutableLiveData()

    val imagesState: ImagesLiveData = ImagesLiveData(application)

    fun loadDetails(id: String, lat: Double, lng: Double) {
        loading.postValue(true)
        job = viewModelScope.launch {
            when (val response = repository.getPlaceDetail(id)) {
                is ResponseWrapper.Success -> {
                    detail = response.value?.detail
                    val placeLat = response.value?.detail?.geometry?.location?.lat
                    val placeLng = response.value?.detail?.geometry?.location?.lng
                    val polyline = getPolyline(repository.getPolyline(placeLat!!, placeLng!!, lat, lng))
                    val fav = repository.existPlace(id)
                    withContext(Dispatchers.Main) {
                        val details: MutableList<IData?> = mutableListOf()
                        response.value.detail.let {
                            details.add(getImages(it.photos))
                            details.add(it)
                            details.add(getMap(it, lat, lng, polyline))
                            details.add(getReviews(it))
                            details.add(it.opening_hours)
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
                        if (imageCacheHelper.hasImage(photo.link)) {
                            photo.path = imageCacheHelper.getFileName(detail.place_id, i)
                        }
                    }
                    repository.savePlaceDetail(detail)
                }
            }
            favorite.postValue(true)
        }
    }

    fun deleteDetails() {
        deletePlace(detail!!.place_id)
        favorite.postValue(false)
    }

    fun saveImage() {
        viewModelScope.launch {
            detail?.let {
                imageCacheHelper.writeImages(it.place_id, it.photos!!)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}