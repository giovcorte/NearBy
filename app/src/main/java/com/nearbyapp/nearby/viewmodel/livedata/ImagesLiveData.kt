package com.nearbyapp.nearby.viewmodel.livedata

import android.app.Application
import androidx.lifecycle.LiveData
import com.nearbyapp.nearby.AppController
import com.nearbyapp.nearby.loader.ImageLoader


class ImagesLiveData(val application: Application): LiveData<ImageLoader.Status>() {

    private val imageLoader = (application as AppController).imageLoader

    private val listener = object : ImageLoader.Listener {
        override fun onWritingStarted() {
            postValue(ImageLoader.Status.WRITING)
        }

        override fun onWritingCompleted() {
            postValue(ImageLoader.Status.COMPLETED)
            value = ImageLoader.Status.IDLE
        }

        override fun onWritingFailed() {
            postValue(ImageLoader.Status.ERROR)
            value = ImageLoader.Status.IDLE
        }
    }

    init {
        value = ImageLoader.Status.IDLE
    }

    override fun onActive() {
        super.onActive()
        imageLoader.registerListener(listener)
    }

    override fun onInactive() {
        imageLoader.unregisterListener()
        super.onInactive()
    }

}