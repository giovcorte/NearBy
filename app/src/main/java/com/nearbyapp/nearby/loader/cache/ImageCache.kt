package com.nearbyapp.nearby.loader.cache

import android.content.Context
import com.nearbyapp.nearby.loader.cache.IImageCache
import kotlin.jvm.Synchronized
import android.graphics.Bitmap
import android.os.Environment
import com.nearbyapp.nearby.loader.cache.ImageCache.CachingStrategy
import com.nearbyapp.nearby.loader.cache.memorycache.MemoryImageCache
import com.nearbyapp.nearby.loader.cache.disklrucache.DiskLruImageCache
import com.nearbyapp.nearby.model.nearby.Photo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ImageCache(context: Context) {

    private val diskLruImageCache: IImageCache
    private val memoryImageCache: IImageCache

    private val outputFolder: File

    enum class CachingStrategy {
        ALL, MEMORY, DISK, NONE
    }

    init {
        memoryImageCache = MemoryImageCache()
        diskLruImageCache = DiskLruImageCache(context, IMAGE_CACHE_NAME, DISK_CACHE_SIZE)
        outputFolder = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath)
    }

    @Synchronized
    operator fun get(s: String?): Bitmap? {
        return if (memoryImageCache.contains(s)) memoryImageCache[s] else diskLruImageCache[s]
    }

    @Synchronized
    fun put(s: String?, data: Bitmap?, cachingStrategy: CachingStrategy?) {
        when (cachingStrategy) {
            CachingStrategy.ALL -> {
                if (memoryImageCache[s] == null) {
                    memoryImageCache.put(s, data)
                }
                if (diskLruImageCache[s] == null) {
                    diskLruImageCache.put(s, data)
                }
            }
            CachingStrategy.DISK -> if (diskLruImageCache[s] == null) {
                diskLruImageCache.put(s, data)
            }
            CachingStrategy.MEMORY -> if (memoryImageCache[s] == null) {
                memoryImageCache.put(s, data)
            }
            CachingStrategy.NONE -> {
            }
        }
    }

    fun contains(key: String?): Boolean {
        return memoryImageCache.contains(key) || diskLruImageCache.contains(key)
    }

    @Synchronized
    fun clear() {
        memoryImageCache.clear()
        diskLruImageCache.clear()
    }

    @Synchronized
    fun dumps(baseName: String, images: List<String>): Boolean {
        images.withIndex().iterator().forEach { link ->
            val key = link.value
            val fileName = getFileName(baseName, link.index)
            try {
                val bitmap = get(key)
                val image = File(outputFolder, fileName)
                val outputStream = FileOutputStream(image)
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
            } catch (e: IOException) {
                return false
            }
        }
        return true
    }

    fun getDownloadedImage(path: String?): File? {
        val image = File(outputFolder.absolutePath + "/$path")
        return if (image.exists()) image else null
    }

    fun exist(path: String?): Boolean {
        val image = File(outputFolder.absolutePath + "/$path")
        return image.exists()
    }

    fun deleteDownloadedImage(id: String) {
        val image = File(outputFolder.absolutePath + "/$id")
        if (image.exists()) {
            image.delete()
        }
    }

    companion object {
        private const val IMAGE_CACHE_NAME = "IImageCache"
        private const val DISK_CACHE_SIZE = 1024 * 1024 * 250 // 250 mb

        fun getFileName(id: String, index: Int): String {
            return "$id-$index"
        }
    }
}