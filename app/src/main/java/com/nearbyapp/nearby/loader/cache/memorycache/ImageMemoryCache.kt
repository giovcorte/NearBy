package com.nearbyapp.nearby.loader.cache.memorycache

import android.graphics.Bitmap
import com.nearbyapp.nearby.loader.cache.IImageCache
import java.util.*

class ImageMemoryCache : IImageCache {

    private val cache = Collections.synchronizedMap(
        LinkedHashMap<String?, Bitmap?>(10, 1.5f, true)
    )

    private var size: Long = 0
    private val limit: Long = Runtime.getRuntime().maxMemory() / 8

    override fun get(key: String): Bitmap? {
        return try {
            if (!cache.containsKey(key)) {
                null
            } else cache[key]
        } catch (e: NullPointerException) {
            null
        }
    }

    override fun contains(key: String): Boolean {
        return cache.containsKey(key)
    }

    @Synchronized
    override fun put(key: String, bitmap: Bitmap) {
        if (cache.containsKey(key)) {
            size -= getSizeInBytes(cache[key])
        }
        cache[key] = bitmap
        size += getSizeInBytes(bitmap)
        checkSize()
    }

    private fun checkSize() {
        if (size > limit) {
            // Least recently accessed item will be the first one iterated
            val iterator: MutableIterator<Map.Entry<String?, Bitmap?>> = cache.entries.iterator()
            while (iterator.hasNext()) {
                val (_, value) = iterator.next()
                size -= getSizeInBytes(value)
                iterator.remove()
                if (size <= limit) {
                    break
                }
            }
        }
    }

    override fun clear() {
        cache.clear()
        size = 0
    }

    private fun getSizeInBytes(bitmap: Bitmap?): Long {
        return if (bitmap == null) {
            0
        } else bitmap.rowBytes.toLong() * bitmap.height
    }

}