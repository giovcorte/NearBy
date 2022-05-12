package com.nearbyapp.nearby.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.databinding.annotations.BindableView
import com.nearbyapp.nearby.R

@BindableView
class ErrorView : LinearLayout {

    val text: TextView
    val image: ImageView
    val button: Button

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
        button = findViewById<Button?>(R.id.button).apply { visibility = GONE }
        this.visibility = View.GONE
    }

}