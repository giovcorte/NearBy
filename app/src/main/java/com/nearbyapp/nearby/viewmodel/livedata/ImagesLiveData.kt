package com.nearbyapp.nearby.viewmodel.livedata

import android.app.Application
import androidx.lifecycle.LiveData
import com.nearbyapp.nearby.AppController
import com.nearbyapp.nearby.components.ImageStorageHelper


class ImagesLiveData(val application: Application): LiveData<ImageStorageHelper.Status>() {

    private val imageStorageHelper = (application as AppController).imageStorageHelper

    private val listener = object : ImageStorageHelper.Listener {
        override fun onWritingStatus(status: ImageStorageHelper.Status) {
            postValue(status)
            when(status) {
                ImageStorageHelper.Status.COMPLETED,
                ImageStorageHelper.Status.ERROR -> {
                    value = ImageStorageHelper.Status.IDLE
                }
                else -> {

                }
            }
        }

    }

    init {
        value = ImageStorageHelper.Status.IDLE
    }

    override fun onActive() {
        super.onActive()
        imageStorageHelper.registerListener(listener)
    }

    override fun onInactive() {
        imageStorageHelper.unregisterListener()
        super.onInactive()
    }

}