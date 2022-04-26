package com.nearbyapp.nearby.loader.cache.disklrucache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.util.Log
import com.nearbyapp.nearby.loader.cache.IImageCache
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException

class DiskLruImageCache(context: Context, uniqueName: String, diskCacheSize: Int) : IImageCache {

    private var diskLruCache: DiskLruCache? = null
    private val compressFormat = CompressFormat.JPEG

    init {
        try {
            diskLruCache = DiskLruCache.open(
                File(context.cacheDir.path + File.separator + uniqueName),
                1,
                1,
                diskCacheSize.toLong()
            )
        } catch (e: IOException) {
            Log.d(TAG, "Cannot open disk cache")
        }
    }

    override fun put(key: String, bitmap: Bitmap) {
        val formattedKey = getDiskLruCacheFormattedString(key)
        var editor: DiskLruCache.Editor? = null
        try {
            editor = diskLruCache!!.edit(formattedKey)
            if (writeBitmapToFile(bitmap, editor)) {
                diskLruCache!!.flush()
                editor.commit()
            } else {
                editor.abort()
            }
        } catch (e: IOException) {
            try {
                editor?.abort()
            } catch (ignored: IOException) {
            }
            Log.d(TAG, "Cannot write disk cache")
        }
    }

    override fun get(key: String): Bitmap? {
        val formattedKey = getDiskLruCacheFormattedString(key)
        var bitmap: Bitmap? = null
        try {
            diskLruCache!![formattedKey].use { snapshot ->
                snapshot?.let {
                    val input = snapshot.getInputStream(0)
                    val buffIn = BufferedInputStream(input, 8 * 1024)
                    bitmap = BitmapFactory.decodeStream(buffIn)
                    it.close()
                }
            }
        } catch (e: IOException) {
            Log.d(TAG, "Cannot read disk cache")
        }
        return bitmap
    }

    override fun contains(key: String): Boolean {
        val formattedKey = getDiskLruCacheFormattedString(key)
        var contained = false
        try {
            diskLruCache!![formattedKey].use { snapshot -> contained = snapshot != null }
        } catch (e: IOException) {
            Log.d(TAG, "Cannot read disk cache")
        }
        return contained
    }

    override fun clear() {
        try {
            diskLruCache!!.delete()
        } catch (e: IOException) {
            Log.d(TAG, "Cannot clear disk cache")
        }
    }

    private fun writeBitmapToFile(bitmap: Bitmap, editor: DiskLruCache.Editor): Boolean {
        try {
            BufferedOutputStream(editor.newOutputStream(0), 1024 * 8).use { out ->
                return bitmap.compress(compressFormat, 100, out)
            }
        } catch (e: IOException) {
            return false
        }
    }

    companion object {
        private const val TAG = "DiskLruImageCache"

        private fun getDiskLruCacheFormattedString(str: String?): String {
            val formatted = str!!.replace("[^a-zA-Z0-9]".toRegex(), "").lowercase()
            return formatted.substring(0, if (formatted.length >= 120) 110 else formatted.length)
        }

    }

}