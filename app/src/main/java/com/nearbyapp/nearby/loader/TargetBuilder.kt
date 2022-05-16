package com.nearbyapp.nearby.loader

import android.graphics.drawable.Drawable
import android.widget.ImageView

class TargetBuilder {

    fun create(view: ImageView?): Target {
        return ImageWrapper(view)
    }

    fun create(view: ImageView?, placeHolder: Drawable): Target {
        return ImageWrapper(view, placeHolder)
    }

}