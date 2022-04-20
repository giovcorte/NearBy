package com.nearbyapp.nearby.loader

import android.graphics.Bitmap

interface Target {

    fun onSuccess(bitmap: Bitmap)

    fun onError()

}