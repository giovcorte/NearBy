package com.nearbyapp.nearby.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.nearbyapp.nearby.AppController
import com.nearbyapp.nearby.components.PreferencesManager
import com.nearbyapp.nearby.repository.RepositoryImpl

abstract class BaseViewModel(application: Application): AndroidViewModel(application) {

    var loading: MutableLiveData<Boolean> = MutableLiveData(true)

    val repository: RepositoryImpl = (application as AppController).repository
    val preferencesManager: PreferencesManager = (application as AppController).preferencesManager!!

    init {
        repository.serviceErrorCallBack?.onReady()
    }

}
