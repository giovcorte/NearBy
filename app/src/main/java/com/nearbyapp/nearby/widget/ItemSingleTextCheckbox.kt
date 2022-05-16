package com.nearbyapp.nearby.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.databinding.annotations.BindWith
import com.databinding.annotations.BindableView
import com.databinding.databinding.IView
import com.nearbyapp.nearby.R

@BindableView
class ItemSingleTextCheckbox: ConstraintLayout, IView {

    @BindWith(paths = ["CachePreference.text:String"])
    val text: TextView
    val checkbox: CheckBox

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        LayoutInflater.from(context).inflate(R.layout.item_cache, this)
        text = findViewById(R.id.text)
        checkbox = findViewById(R.id.checkBox)
    }

    override fun name(): String {
        return "ItemSingleTextCheckbox"
    }
}