package com.nearbyapp.nearby.loader.cache

import android.content.Context
import android.graphics.Bitmap
import com.nearbyapp.nearby.loader.Request
import com.nearbyapp.nearby.loader.cache.diskcache.AsyncImageDiskCache
import com.nearbyapp.nearby.loader.cache.diskcache.DiskCache
import com.nearbyapp.nearby.loader.cache.memorycache.ImageMemoryCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageCache(context: Context) : IImageCache {

    private val diskImageCache = AsyncImageDiskCache(DiskCache(File(context.cacheDir.path + File.separator + "imagescache"), 1024 * 1024 * 200, 1))
    private val memoryImageCache = ImageMemoryCache()

    enum class CachingStrategy {
        ALL, MEMORY, DISK, NONE
    }

    override suspend fun get(request: Request): Bitmap? {
        val key = request.cachingKey()
        return if (memoryImageCache.contains(key)) memoryImageCache[key] else diskImageCache.get(key)
    }

    override suspend fun put(request: Request, bitmap: Bitmap) {
        val key = request.cachingKey()
        when (request.cachingStrategy()) {
            CachingStrategy.ALL -> {
                memoryImageCache.put(key, bitmap)
                diskImageCache.put(key, bitmap)
            }
            CachingStrategy.DISK -> if (diskImageCache.get(key) == null) {
                diskImageCache.put(key, bitmap)
            }
            CachingStrategy.MEMORY -> if (memoryImageCache[key] == null) {
                memoryImageCache.put(key, bitmap)
            }
            CachingStrategy.NONE -> { }
        }
    }

    override fun contains(request: Request): Boolean {
        val key = request.cachingKey()
        return memoryImageCache.contains(key) || diskImageCache.contains(key)
    }

    override suspend fun clear() {
        memoryImageCache.clear()
        diskImageCache.clear()
    }

    override suspend fun dumps(request: Request, file: File): Boolean {
        return try {
            withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                val bitmap = get(request)
                writeBitmap(bitmap, file)
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