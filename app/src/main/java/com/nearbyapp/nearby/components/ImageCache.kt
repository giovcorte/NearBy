package com.nearbyapp.nearby.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.*
import java.util.*

class ImageCache(destinationFolder: File) {

    private val cache: MutableMap<String, Bitmap> = Collections.synchronizedMap(LinkedHashMap(10, 1.5f, true))

    private var folder: File = destinationFolder

    private var size: Long = 0
    private var limit: Long = Runtime.getRuntime().maxMemory() / 4

    operator fun get(id: String): Bitmap? {
        return cache[id]
    }

    operator fun contains(key: String): Boolean {
        return cache.containsKey(key)
    }

    fun put(id: String, bitmap: Bitmap) {
        if (cache.containsKey(id)) {
            size -= getSizeInBytes(cache[id])
        }
        cache[id] = bitmap
        size += getSizeInBytes(bitmap)
        checkSize()
    }

    @Throws(IOException::class)
    fun write(name: String, bitmap: Bitmap) {
        val image = File(folder, name)
        val outputStream = FileOutputStream(image)
        compress(bitmap)?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
    }

    fun folder(): String {
        return folder.absolutePath
    }

    private fun compress(bitmap: Bitmap): Bitmap? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes: ByteArray = stream.toByteArray()

        // Decode image size
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val stream1: InputStream = ByteArrayInputStream(bytes)
        BitmapFactory.decodeStream(stream1, null, options)
        stream1.close()
        // Scale image in order to reduce memory consumption
        val requiredSize = 300
        var widthTmp = options.outWidth
        var heightTmp = options.outHeight
        var scale = 1
        while (widthTmp / 2 >= requiredSize && heightTmp / 2 >= requiredSize) {
            widthTmp /= 2
            heightTmp /= 2
            scale *= 2
        }
        // Decode with current scale values
        val options1 = BitmapFactory.Options()
        options1.inSampleSize = scale
        val stream2: InputStream = ByteArrayInputStream(bytes)
        val finalBitmap = BitmapFactory.decodeStream(stream2, null, options1)
        stream2.close()
        return finalBitmap
    }

    private fun checkSize() {
        if (size > limit) {
            // Least recently accessed item will be the first one iterated
            val iterator: MutableIterator<Map.Entry<String, Bitmap>> = cache.entries.iterator()
            while (iterator.hasNext()) {
                val (_, value) = iterator.next()
                size -= getSizeInBytes(value)
                iterator.remove()
                if (size <= limit) {
                    break
                }
            }
        }
    }

    fun clear() {
        cache.clear()
        size = 0
    }

    private fun getSizeInBytes(bitmap: Bitmap?): Long {
        return if (bitmap == null) {
            0
        } else bitmap.rowBytes.toLong() * bitmap.height
    }

}