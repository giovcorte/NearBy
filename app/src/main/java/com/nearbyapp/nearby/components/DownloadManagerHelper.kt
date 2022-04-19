package com.nearbyapp.nearby.components

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.nearbyapp.nearby.model.detail.Detail
import java.io.File

class DownloadManagerHelper(val application: Application) {

    private val downloadManager = application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun downloadImage(detail: Detail?): DownloadRef {
        detail?.thumbnail?.let {
            val imageUri = Uri.parse(it)
            val request = DownloadManager.Request(imageUri)
            request.setTitle("NearBy")
            request.setDescription("Download immagine per ${detail.place_name}")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            request.setDestinationInExternalFilesDir(
                application,
                Environment.DIRECTORY_DOWNLOADS,
                detail.place_id
            )
            return DownloadRef(downloadManager.enqueue(request), detail.place_name, DownloadRef.VALID)
        }
        return DownloadRef.invalid()
    }

    fun getImage(id: String): File? {
        val image = File(application.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/$id")
        return if (image.exists()) {
            image
        } else {
            null
        }
    }

    fun deleteImage(id: String) {
        val image = File(application.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/$id")
        if (image.exists()) {
            image.delete()
        }
    }

    data class DownloadRef(val id: Long, val name: String, val status: String) {
        companion object {
            const val INVALID = "invalid"
            const val VALID = "valid"

            fun invalid(): DownloadRef {
                return DownloadRef(0, "", INVALID)
            }
        }
    }
}