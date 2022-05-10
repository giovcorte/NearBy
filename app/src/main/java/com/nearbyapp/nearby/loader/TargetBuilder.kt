package com.nearbyapp.nearby.loader

import android.graphics.Bitmap
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.View.GONE
import android.widget.ImageView

class TargetBuilder {

    fun create(view: ImageView?): Target {
        return object : Target {
            override fun onProcessing(cached: Boolean) {

            }

            override fun onSuccess(bitmap: Bitmap) {
                view?.setImageBitmap(bitmap)
            }

            override fun onError() {
                view?.visibility = GONE
            }

        }
    }

    fun create(view: ImageView?, placeHolder: Drawable): Target {
        return object : Target {
            override fun onProcessing(cached: Boolean) {
                if (!cached) {
                    view?.setImageDrawable(placeHolder)
                    if (placeHolder is Animatable) placeHolder.start()
                }
            }

            override fun onSuccess(bitmap: Bitmap) {
                view?.setImageBitmap(bitmap)
            }

            override fun onError() {
                view?.visibility = GONE
            }
        }
    }
}