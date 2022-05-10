package com.nearbyapp.nearby.components

import com.nearbyapp.nearby.loader.cache.ImageCache
import com.nearbyapp.nearby.model.nearby.Photo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ImageStorageHelper(private val folder: File, private val imageCache: ImageCache) {

    private var listener: Listener? = null

    interface Listener {
        fun onWritingStatus(status: Status)
    }

    fun getImageFile(id: String?): File? {
        val image = File("${folder.absolutePath}/$id")
        return if (image.exists()) image else null
    }

    fun hasImageFile(id: String?): Boolean {
        val image = File("${folder.absolutePath}/$id")
        return image.exists() && image.isFile
    }

    fun hasImageFile(id: String, index: Int): Boolean {
        val image = File("${folder.absolutePath}/${getImageId(id, index)}")
        return image.exists()
    }

    fun deleteStoredImage(id: String) {
        val image = File("${folder.absolutePath}/$id")
        if (image.exists()) {
            image.delete()
        }
    }

    fun getImageId(id: String, index: Int): String {
        return "$id-$index"
    }

    private suspend fun storeImages(baseName: String, images: List<String>): Boolean {
        images.withIndex().iterator().forEach { link ->
            val key = link.value
            val fileName = getImageId(baseName, link.index)

            val success = imageCache.dumps(key, fileName, folder)

            if (!success){
                return false
            }
        }
        return true
    }

    fun storeImages(baseName: String, images: List<Photo>) {
        listener?.onWritingStatus(Status.WRITING)
        CoroutineScope(Dispatchers.Main).launch {
            val success: Boolean = withContext(Dispatchers.IO) {
                storeImages(baseName, images.map { it.link })
            }
            if (success) {
                listener?.onWritingStatus(Status.COMPLETED)
            } else {
                listener?.onWritingStatus(Status.ERROR)
            }
        }
    }

    fun registerListener(listener: Listener) {
        this.listener = listener
    }

    fun unregisterListener() {
        this.listener = null
    }

    enum class Status {
        IDLE,
        WRITING,
        COMPLETED,
        ERROR
    }

}