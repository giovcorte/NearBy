package com.nearbyapp.nearby.loader.cache

import android.graphics.Bitmap
import com.nearbyapp.nearby.loader.Request
import java.io.File

interface IImageCache {

    suspend fun get(request: Request) : Bitmap?

    suspend fun put(request: Request, bitmap: Bitmap)

    suspend fun clear()

    suspend fun dumps(request: Request, file: File) : Boolean

    fun contains(request: Request) : Boolean

}