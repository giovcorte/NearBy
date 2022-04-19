package com.nearbyapp.nearby.components

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.nearbyapp.nearby.Utils
import com.nearbyapp.nearby.model.nearby.Photo
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.lang.Exception

object ImageLoader {

    fun load(imageView: ImageView?, photo: Photo, imageCacheHelper: ImageCacheHelper) {
        imageCacheHelper.getDownloadedImage(photo.path)?.let {
            Picasso.get().load(it).fit().centerCrop().into(imageView)
        } ?: run {
            Picasso.get().load(photo.link).into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    bitmap?.let {
                        imageCacheHelper.addImage(photo.link, it)
                        imageView?.setImageBitmap(it)
                    }
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) { }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) { }
            })
        }
    }

    fun load(view: ImageView?, data: String) {
        if (Utils.isNumber(data)) {
            Picasso.get().load(data.toInt()).fit().into(view)
        } else {
            Picasso.get().load(data).centerCrop().fit().into(view)
        }
    }
}