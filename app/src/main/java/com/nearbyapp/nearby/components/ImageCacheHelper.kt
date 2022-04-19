package com.nearbyapp.nearby.components

import android.graphics.Bitmap
import com.nearbyapp.nearby.model.nearby.Photo
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException

class ImageCacheHelper(private val cache: ImageCache) {

    interface Listener {
        fun onWritingStarted()
        fun onWritingCompleted()
        fun onWritingFailed()
    }

    private var listener: Listener? = null
    private var job: Job? = null

    fun addImage(url: String, image: Bitmap) {
        cache.put(url, image)
    }

    @Synchronized
    fun writeImages(baseName: String, images: List<Photo>) {
        listener?.onWritingStarted()
        job = CoroutineScope(Dispatchers.Main).launch {
            val success: Boolean = withContext(Dispatchers.IO) { write(baseName, images) }
            if (success) {
                 listener?.onWritingCompleted()
            } else {
                listener?.onWritingFailed()
            }
        }
    }

    private fun write(baseName: String, images: List<Photo>): Boolean {
        images.map { it.link }.withIndex().iterator().forEach { link ->
            val imageKey = link.value
            val fileName = getFileName(baseName, link.index)
            try {
                write(fileName, imageKey)
            } catch (e: IOException) {
                return false
            }
        }
        return true
    }

    @Throws(IOException::class)
    fun write(fileName: String, image: String) {
        val bitmap = cache[image]
        bitmap?.let { cache.write(fileName, it) }
    }

    fun getDownloadedImage(path: String?): File? {
        val image = File(cache.folder() + "/$path")
        return if (image.exists()) image else null
    }

    fun deleteDownloadedImage(id: String) {
        val image = File(cache.folder() + "/$id")
        if (image.exists()) {
            image.delete()
        }
    }

    fun getFileName(id: String, index: Int): String {
        return "$id-$index"
    }

    fun hasImage(link: String): Boolean {
        return cache.contains(link)
    }

    fun registerListener(listener: Listener) {
        this.listener = listener
    }

    fun unregisterListener() {
        this.listener = null
    }

    fun abortWritingImages() {
        job?.cancel()
        listener?.onWritingCompleted()
    }

    enum class Status {
        IDLE,
        WRITING,
        COMPLETED,
        ERROR
    }

}