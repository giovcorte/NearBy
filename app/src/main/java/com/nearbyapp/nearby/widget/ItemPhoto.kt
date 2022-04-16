package com.nearbyapp.nearby.widget

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import com.databinding.databinding.IView
import android.view.LayoutInflater
import android.widget.ImageView
import com.databinding.annotations.BindWith
import com.databinding.annotations.BindableView
import com.nearbyapp.nearby.R

@BindableView
class ItemPhoto(context: Context) : ConstraintLayout(context), IView {

    @BindWith(paths = ["Photo.link:String"])
    var image: ImageView

    override fun name(): String {
        return "ItemPhoto"
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.item_photo, this)
        image = findViewById(R.id.image)
    }
}