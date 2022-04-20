package com.nearbyapp.nearby.loader.fetcher

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.core.content.res.ResourcesCompat
import com.nearbyapp.nearby.loader.ImageResult
import com.nearbyapp.nearby.loader.Request

class ResourceFetcher: ImageFetcher() {

    override fun fetch(request: Request): ImageResult<Bitmap> {
        try {
            val resource = request.source().toInt()

            var bitmap = BitmapFactory.decodeResource(request.context().resources, resource)

            if (bitmap != null) {
                return ImageResult.Success(bitmap)
            }

            val drawable = ResourcesCompat.getDrawable(request.context().resources, resource, null)
            val canvas = Canvas()

            if (drawable != null) {
                bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                canvas.setBitmap(bitmap)
                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                drawable.draw(canvas)
            }

            return ImageResult.Success(bitmap!!)
        } catch (e: Exception) {
            return ImageResult.Error()
        }
    }
}