package com.nearbyapp.nearby.widget

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.TextView
import android.widget.SeekBar
import android.view.LayoutInflater
import com.databinding.annotations.BindWith
import com.databinding.annotations.BindableView
import com.databinding.databinding.IView
import com.nearbyapp.nearby.R

@BindableView
class ItemTextSeekBar : ConstraintLayout, IView {
    var text: TextView
    @BindWith(paths = ["RadiusPreference.maxText:String"])
    var max: TextView
    @BindWith(paths = ["RadiusPreference.minText:String"])
    var min: TextView
    @BindWith(paths = ["RadiusPreference.currentText:String"])
    var current: TextView
    var seekBar: SeekBar

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    init {
        LayoutInflater.from(context).inflate(R.layout.item_text_seek_bar, this)
        text = findViewById(R.id.text)
        max = findViewById(R.id.max)
        min = findViewById(R.id.min)
        current = findViewById(R.id.current)
        seekBar = findViewById(R.id.seekBar)
    }

    override fun name(): String {
        return "ItemTextSeekBar"
    }
}