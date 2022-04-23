package com.nearbyapp.nearby.loader.cache

import android.content.Context
import android.graphics.Bitmap
import com.nearbyapp.nearby.loader.cache.diskcache.ImageDiskCache
import com.nearbyapp.nearby.loader.cache.memorycache.ImageMemoryCache
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageCache(context: Context) {

    private val diskLruImageCache: IImageCache
    private val memoryImageCache: IImageCache

    enum class CachingStrategy {
        ALL, MEMORY, DISK, NONE
    }

    init {
        memoryImageCache = ImageMemoryCache()
        diskLruImageCache = ImageDiskCache(File(context.cacheDir.path + File.separator + "diskcache"), DISK_CACHE_SIZE)
    }

    operator fun get(s: String): Bitmap? {
        return if (memoryImageCache.contains(s)) memoryImageCache[s] else diskLruImageCache[s]
    }

    @Synchronized
    fun put(s: String, data: Bitmap, cachingStrategy: CachingStrategy?) {
        when (cachingStrategy) {
            CachingStrategy.ALL -> {
                if (!memoryImageCache.contains(s)) {
                    memoryImageCache.put(s, data)
                }
                if (!diskLruImageCache.contains(s)) {
                    diskLruImageCache.put(s, data)
                }
            }
            CachingStrategy.DISK -> if (diskLruImageCache[s] == null) {
                if (!diskLruImageCache.contains(s)) {
                    diskLruImageCache.put(s, data)
                }
            }
            CachingStrategy.MEMORY -> if (memoryImageCache[s] == null) {
                if (!memoryImageCache.contains(s)) {
                    memoryImageCache.put(s, data)
                }
            }
            CachingStrategy.NONE -> {
            }
        }
    }

    fun contains(key: String): Boolean {
        return memoryImageCache.contains(key) || diskLruImageCache.contains(key)
    }

    @Synchronized
    fun clear() {
        memoryImageCache.clear()
        diskLruImageCache.clear()
    }

    @Synchronized
    fun dumps(imageKey: String, fileName: String, outputFolder: File): Boolean {
        return try {
            val bitmap = get(imageKey)
            val image = File(outputFolder, fileName)
            val outputStream = FileOutputStream(image)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: IOException) {
            false
        }
    }

    companion object {
        private const val DISK_CACHE_SIZE: Long = 1024 * 1024 * 250 // 250 mb
    }
}