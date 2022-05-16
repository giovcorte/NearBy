package com.nearbyapp.nearby.loader

import android.graphics.Bitmap

interface Target {

    fun onProcessing(cached: Boolean)

    fun onSuccess(bitmap: Bitmap)

    fun onError()

    fun getId() : Int

}