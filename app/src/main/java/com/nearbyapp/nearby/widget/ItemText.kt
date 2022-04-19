package com.nearbyapp.nearby.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.databinding.annotations.BindWith
import com.databinding.annotations.BindableView
import com.databinding.databinding.IView
import com.nearbyapp.nearby.R

@BindableView
class ItemText: ConstraintLayout, IView {

    @BindWith(paths = ["TextWrapper.text:String"])
    val text: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.item_text, this)
        this.text = findViewById(R.id.text)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun name(): String {
        return "ItemText"
    }
}