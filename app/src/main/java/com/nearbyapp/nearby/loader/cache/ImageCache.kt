package com.nearbyapp.nearby.loader.cache

import android.content.Context
import android.graphics.Bitmap
import com.nearbyapp.nearby.loader.cache.diskcache.AsyncImageDiskCache
import com.nearbyapp.nearby.loader.cache.diskcache.DiskCache
import com.nearbyapp.nearby.loader.cache.memorycache.ImageMemoryCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageCache(context: Context) {

    private val diskLruImageCache = AsyncImageDiskCache(DiskCache(File(context.cacheDir.path + File.separator + "imagescache"), 1024 * 1024 * 200, 1))
    private val memoryImageCache = ImageMemoryCache()

    enum class CachingStrategy {
        ALL, MEMORY, DISK, NONE
    }

    suspend fun get(s: String): Bitmap? {
        return if (memoryImageCache.contains(s)) memoryImageCache[s] else diskLruImageCache.get(s)
    }

    suspend fun put(s: String, data: Bitmap, cachingStrategy: CachingStrategy?) {
        when (cachingStrategy) {
            CachingStrategy.ALL -> {
                memoryImageCache.put(s, data)
                diskLruImageCache.put(s, data)
            }
            CachingStrategy.DISK -> if (diskLruImageCache.get(s) == null) {
                diskLruImageCache.put(s, data)
            }
            CachingStrategy.MEMORY -> if (memoryImageCache[s] == null) {
                memoryImageCache.put(s, data)
            }
            CachingStrategy.NONE -> { }
        }
    }

    fun contains(key: String): Boolean {
        return memoryImageCache.contains(key) || diskLruImageCache.contains(key)
    }

    @Synchronized
    fun clear() {
        memoryImageCache.clear()
    }

    suspend fun dumps(imageKey: String, fileName: String, outputFolder: File): Boolean {
        return try {
            withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                val bitmap = get(imageKey)
                val image = File(outputFolder, fileName)
                writeBitmap(bitmap, image)
            }
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun writeBitmap(bitmap: Bitmap?, imageFile: File) : Boolean {
        return try {
            val outputStream = FileOutputStream(imageFile)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            true
        } catch (_: Exception) {
            false
        }
    }

}