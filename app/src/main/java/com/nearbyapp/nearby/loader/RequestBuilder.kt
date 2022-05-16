package com.nearbyapp.nearby.loader

import android.app.Application
import com.nearbyapp.nearby.loader.cache.ImageCache
import java.io.File

class RequestBuilder(val application: Application) {

    fun create(source: String): Request {
        return RequestWrapper(source)
    }

    fun create(source: Int): Request {
        return RequestWrapper(source.toString(), null, ImageCache.CachingStrategy.NONE)
    }

    fun create(source: File): Request {
        return RequestWrapper(source.absolutePath, null, ImageCache.CachingStrategy.MEMORY)
    }
}