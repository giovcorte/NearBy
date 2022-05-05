package com.nearbyapp.nearby.loader.cache.diskcache

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.nearbyapp.nearby.loader.cache.IImageCache
import java.io.FileInputStream
import java.io.FileOutputStream

class ImageDiskCache(private val diskCache: DiskCache) : IImageCache {

    override fun get(key: String): Bitmap? {
        val realKey = formatKey(key)
        var bitmap: Bitmap? = null
        diskCache.get(realKey)?.let { snapshot ->
            bitmap = BitmapFactory.decodeStream(FileInputStream(snapshot.file()))
            snapshot.close()
        }
        return bitmap
    }

    override fun contains(key: String): Boolean {
        return diskCache.get(formatKey(key)) != null
    }

    override fun put(key: String, bitmap: Bitmap) {
        diskCache.edit(formatKey(key))?.let { editor ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(editor.file()))
            editor.commit()
        }
    }

    override fun clear() {

    }

    private fun formatKey(str: String?): String {
        val formatted = str!!.replace("[^a-zA-Z0-9]".toRegex(), "").lowercase()
        return formatted.substring(0, if (formatted.length >= 120) 110 else formatted.length)
    }

}