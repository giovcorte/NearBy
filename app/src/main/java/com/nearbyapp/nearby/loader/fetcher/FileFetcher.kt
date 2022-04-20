package com.nearbyapp.nearby.loader.fetcher

import android.graphics.Bitmap
import com.nearbyapp.nearby.loader.ImageResult
import com.nearbyapp.nearby.loader.Request
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class FileFetcher: ImageFetcher() {

    override fun fetch(request: Request): ImageResult<Bitmap> {
        try {
            val inputStream = FileInputStream(File(request.source()))
            val byteArrayOutputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream.read(buffer).also { len = it } > -1) {
                byteArrayOutputStream.write(buffer, 0, len)
            }
            byteArrayOutputStream.flush()
            inputStream.close()
            val bitmap = decodeByteArray(
                byteArrayOutputStream.toByteArray(),
                400
            )
            byteArrayOutputStream.close()

            return ImageResult.Success(bitmap!!)
        } catch (e: Exception) {
            return ImageResult.Error()
        }
    }

}