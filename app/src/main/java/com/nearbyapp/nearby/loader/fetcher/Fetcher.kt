package com.nearbyapp.nearby.loader.fetcher

import android.graphics.Bitmap
import com.nearbyapp.nearby.loader.ImageResult
import com.nearbyapp.nearby.loader.Request

interface Fetcher {

    fun fetch(request: Request): ImageResult<Bitmap>

}