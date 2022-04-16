package com.nearbyapp.nearby.widget

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.TextView
import android.widget.CheckBox
import android.view.LayoutInflater
import com.databinding.annotations.BindWith
import com.databinding.annotations.BindableView
import com.databinding.databinding.IView
import com.nearbyapp.nearby.R

@BindableView
class ItemTextCheckbox : ConstraintLayout, IView {

    @BindWith(paths = ["TravelModePreference.first.text:String"])
    var text: TextView
    var checkBox: CheckBox
    @BindWith(paths = ["TravelModePreference.second.text:String"])
    var text1: TextView
    var checkBox1: CheckBox

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.item_text_checkbox, this)
        text = findViewById(R.id.text)
        checkBox = findViewById(R.id.checkBox)
        text1 = findViewById(R.id.text1)
        checkBox1 = findViewById(R.id.checkBox1)
    }

    override fun name(): String {
        return "ItemTextCheckbox"
    }

}