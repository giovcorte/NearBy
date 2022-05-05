package com.nearbyapp.nearby.loader.cache

import android.content.Context
import android.graphics.Bitmap
import com.nearbyapp.nearby.loader.cache.diskcache.DiskCache
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
        diskLruImageCache = ImageDiskCache(DiskCache(File(context.cacheDir.path + File.separator + "imagescache"), 1024 * 1024 * 20, 1))
    }

    operator fun get(s: String): Bitmap? {
        return /*if (memoryImageCache.contains(s)) memoryImageCache[s] else*/ diskLruImageCache[s]
    }

    fun put(s: String, data: Bitmap, cachingStrategy: CachingStrategy?) {
        when (cachingStrategy) {
            CachingStrategy.ALL -> {
                /*
                if (!memoryImageCache.contains(s)) {
                    memoryImageCache.put(s, data)
                }

                 */

                diskLruImageCache.put(s, data)
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
        return /*memoryImageCache.contains(key) ||*/ diskLruImageCache.contains(key)
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
}