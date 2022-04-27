package com.nearbyapp.nearby.loader.cache.disklrucache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import com.nearbyapp.nearby.loader.cache.IImageCache
import java.io.File
import java.io.IOException
import java.io.OutputStream

class DiskLruImageCache(context: Context, uniqueName: String) : IImageCache {

    private val size: Long = 1024 * 1024 * 250 // 20 mb

    private var diskLruCache: DiskLruCache = DiskLruCache(
        File(context.cacheDir.path + File.separator + uniqueName),
        1,
        1,
        size
    )

    override fun put(key: String, bitmap: Bitmap) {
        val formattedKey = getDiskLruCacheFormattedString(key)
        diskLruCache.edit(formattedKey)?.let {
            if (writeBitmapToFile(bitmap, it.newOutputStream(0))) {
                diskLruCache.flush()
                it.commit()
            } else {
                it.abort()
            }
        }
    }

    override fun get(key: String): Bitmap? {
        val formattedKey = getDiskLruCacheFormattedString(key)
        var bitmap: Bitmap? = null
        diskLruCache[formattedKey].use { snapshot ->
            snapshot?.let {
                bitmap = BitmapFactory.decodeStream(snapshot.getInputStream(0))
                it.close()
            }
        }
        return bitmap
    }

    override fun contains(key: String): Boolean {
        val formattedKey = getDiskLruCacheFormattedString(key)
        var contained: Boolean
        diskLruCache[formattedKey].use { snapshot -> contained = snapshot != null }
        return contained
    }

    override fun clear() {
        diskLruCache.delete()
    }

    private fun writeBitmapToFile(bitmap: Bitmap, outputStream: OutputStream): Boolean {
        return try {
            bitmap.compress(CompressFormat.PNG, 100, outputStream)
        } catch (e: IOException) {
            false
        }
    }

    private fun getDiskLruCacheFormattedString(str: String?): String {
        val formatted = str!!.replace("[^a-zA-Z0-9]".toRegex(), "").lowercase()
        return formatted.substring(0, if (formatted.length >= 120) 110 else formatted.length)
    }

}