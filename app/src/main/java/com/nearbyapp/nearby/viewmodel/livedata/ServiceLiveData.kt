package com.nearbyapp.nearby.viewmodel.livedata

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.nearbyapp.nearby.AppController
import com.nearbyapp.nearby.repository.Repository

class ServiceLiveData(application: Application): MutableLiveData<Boolean>() {

    private val serviceCallback = object: Repository.ServiceCallBack {
        override fun onReady() {
            postValue(true)
        }

        override fun onError() {
            postValue(false)
        }
    }

    init {
        postValue(true)
        (application as AppController).repository.registerServiceCallback(serviceCallback)
    }

    fun isActive(): Boolean {
        value?.let {
            return it
        }
        return true
    }

}