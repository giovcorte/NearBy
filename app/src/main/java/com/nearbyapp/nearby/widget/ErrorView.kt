package com.nearbyapp.nearby.widget

import android.widget.LinearLayout
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.databinding.annotations.BindableView
import com.nearbyapp.nearby.R

@BindableView
class ErrorView : LinearLayout {

    val text: TextView
    val image: ImageView

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.error_view, this)
        text = findViewById(R.id.text)
        image = findViewById(R.id.image)
        this.visibility = View.GONE
    }

}