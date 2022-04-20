package com.nearbyapp.nearby.loader

import android.content.Context
import com.nearbyapp.nearby.loader.cache.ImageCache

interface Request {

    fun source(): String
    fun target(): Target
    fun context(): Context
    fun caching(): ImageCache.CachingStrategy

}