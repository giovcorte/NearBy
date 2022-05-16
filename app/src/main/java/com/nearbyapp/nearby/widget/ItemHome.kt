package com.nearbyapp.nearby.widget

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.databinding.annotations.BindWith
import com.databinding.annotations.BindableView
import com.databinding.databinding.IView
import com.nearbyapp.nearby.R

@BindableView
class ItemHome(context: Context) : ConstraintLayout(context), IView {

    @BindWith(paths = ["HomeCategory.category:String"])
    var text: TextView
    @BindWith(paths = ["HomeCategory.image:String"])
    var image: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.item_home, this, true)
        text = findViewById(R.id.text)
        image = findViewById(R.id.image)
    }

    override fun name(): String {
        return "ItemHome"
    }
}