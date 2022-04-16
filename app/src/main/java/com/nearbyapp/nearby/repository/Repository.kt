package com.nearbyapp.nearby.repository

interface Repository {

    interface ServiceCallBack {
        fun onReady();
        fun onError()
    }

}