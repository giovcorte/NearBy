package com.nearbyapp.nearby.loader.fetcher

import android.graphics.Bitmap
import com.nearbyapp.nearby.loader.ImageResult
import com.nearbyapp.nearby.loader.Request
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class URLFetcher: ImageFetcher() {

    override fun fetch(request: Request): ImageResult<Bitmap> {
        try {
            // InputStream from url
            val imageUrl = URL(request.source())
            val conn = imageUrl.openConnection() as HttpURLConnection
            val inputStream = conn.inputStream

            // Creating byteArrayOutputStream to decode bitmap and cache it

            // Creating byteArrayOutputStream to decode bitmap and cache it
            val outputStreamUrl = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var len: Int
            while (inputStream.read(buffer).also { len = it } > -1) {
                outputStreamUrl.write(buffer, 0, len)
            }
            outputStreamUrl.flush()
            inputStream.close()
            conn.disconnect()

            // Decode byte[] to bitmap, but not from the cached file. Doing so permit us to get the bitmap also if memory is full

            // Decode byte[] to bitmap, but not from the cached file. Doing so permit us to get the bitmap also if memory is full
            val bitmap = decodeByteArray(outputStreamUrl.toByteArray(), 300)
            outputStreamUrl.close()

            return ImageResult.Success(bitmap!!)
        } catch (e: Exception) {
            return ImageResult.Error()
        }
    }

}