package com.nearbyapp.nearby.loader

import android.graphics.Bitmap

sealed class ImageResult<out T> {
    data class Success(val value: Bitmap): ImageResult<Bitmap>()
    data class Error(val value: Bitmap? = null) : ImageResult<Nothing>()
}