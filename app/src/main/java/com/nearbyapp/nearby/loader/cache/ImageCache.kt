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

class ImageCache(
    context: Context,
    diskSize: Long,
    appVersion: Int
) : IImageCache {

    private val diskImageCache = AsyncImageDiskCache(DiskCache(File(context.cacheDir.path + File.separator + "imagescache"), diskSize, appVersion))
    private val memoryImageCache = ImageMemoryCache()

    override suspend fun get(request: Request): Bitmap? {
        val key = request.cachingKey()
        return if (memoryImageCache.contains(key)) memoryImageCache[key] else diskImageCache.get(key)
    }

    override suspend fun put(request: Request, bitmap: Bitmap) : Boolean {
        val key = request.cachingKey()
        var result = true
        when (request.cachingStrategy()) {
            IImageCache.CachingStrategy.ALL -> {
                memoryImageCache.put(key, bitmap)
                result = diskImageCache.put(key, bitmap)
            }
            IImageCache.CachingStrategy.DISK -> if (diskImageCache.get(key) == null) {
                result = diskImageCache.put(key, bitmap)
            }
            IImageCache.CachingStrategy.MEMORY -> if (memoryImageCache[key] == null) {
                memoryImageCache.put(key, bitmap)
            }
            else -> { }
        }
        return result
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