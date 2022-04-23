package com.nearbyapp.nearby.loader.cache

import android.graphics.Bitmap

interface IImageCache {
    operator fun get(key: String): Bitmap?
    operator fun contains(key: String): Boolean
    fun put(key: String, bitmap: Bitmap)
    fun clear()
}