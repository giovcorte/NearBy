package com.nearbyapp.nearby.loader

import android.graphics.Bitmap
import android.widget.ImageView

class TargetBuilder {

    fun create(view: ImageView?): Target {
        return object : Target {
            override fun onSuccess(bitmap: Bitmap) {
                view?.setImageBitmap(bitmap)
            }

            override fun onError() {
                TODO("Not yet implemented")
            }

        }
    }
}