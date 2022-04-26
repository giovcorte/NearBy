package com.nearbyapp.nearby.loader.cache.diskcache

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.nearbyapp.nearby.loader.cache.IImageCache

class ImageDiskCache(private val diskCache: DiskCache): IImageCache {

    override fun get(key: String): Bitmap? {
        var bitmap: Bitmap? = null
        diskCache.get(key)?.let {
            it.inputStream().use { ins ->
                bitmap = BitmapFactory.decodeStream(ins)
                it.close()
            }
        }
        return bitmap
    }

    override fun contains(key: String): Boolean {
        return diskCache.contains(key)
    }

    override fun put(key: String, bitmap: Bitmap) {
        diskCache.edit(key)?.let { editor ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, editor.outputStream())
            editor.commit()
        }
    }

    override fun clear() {
        diskCache.clear()
    }
}