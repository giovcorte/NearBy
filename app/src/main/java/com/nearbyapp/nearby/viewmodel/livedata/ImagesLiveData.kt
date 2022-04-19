package com.nearbyapp.nearby.viewmodel.livedata

import android.app.Application
import androidx.lifecycle.LiveData
import com.nearbyapp.nearby.AppController
import com.nearbyapp.nearby.components.ImageCacheHelper


class ImagesLiveData(val application: Application): LiveData<ImageCacheHelper.Status>() {

    private val imageCacheHelper = (application as AppController).imageCacheHelper

    private val listener = object : ImageCacheHelper.Listener {
        override fun onWritingStarted() {
            postValue(ImageCacheHelper.Status.WRITING)
        }

        override fun onWritingCompleted() {
            postValue(ImageCacheHelper.Status.COMPLETED)
            value = ImageCacheHelper.Status.IDLE
        }

        override fun onWritingFailed() {
            postValue(ImageCacheHelper.Status.ERROR)
            value = ImageCacheHelper.Status.IDLE
        }
    }

    init {
        value = ImageCacheHelper.Status.IDLE
    }

    override fun onActive() {
        super.onActive()
        imageCacheHelper.registerListener(listener)
    }

    override fun onInactive() {
        imageCacheHelper.unregisterListener()
        super.onInactive()
    }

}