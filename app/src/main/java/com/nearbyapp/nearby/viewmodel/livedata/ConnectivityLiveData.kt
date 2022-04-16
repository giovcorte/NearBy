package com.nearbyapp.nearby.viewmodel.livedata

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.LiveData

class ConnectivityLiveData(application: Application): LiveData<Boolean>() {

    private var connectivityManager: ConnectivityManager = (application.getSystemService(ConnectivityManager::class.java) as ConnectivityManager)

    init {
        postValue(isInternetAvailable())
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            postValue(isInternetAvailable())
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            postValue(isInternetAvailable())
        }
    }

    override fun onActive() {
        super.onActive()
        val builder = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
    }

    override fun onInactive() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        super.onInactive()
    }

    private fun isInternetAvailable(): Boolean {
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        val result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }

        return result
    }

    fun isActive(): Boolean {
        value?.let {
            return it
        }
        return true
    }

}