package com.nearbyapp.nearby.loader

import android.graphics.drawable.Drawable
import android.widget.ImageView

class TargetBuilder {

    fun create(view: ImageView?, source: String): Target {
        return ImageWrapper(view, source)
    }

    fun create(view: ImageView?, source: String, placeHolder: Drawable): Target {
        return ImageWrapper(view, source, placeHolder)
    }

}