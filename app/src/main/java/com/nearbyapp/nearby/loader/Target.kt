package com.nearbyapp.nearby.loader

import android.graphics.Bitmap

interface Target {

    fun onProcessing()

    fun onSuccess(bitmap: Bitmap)

    fun onError()

}