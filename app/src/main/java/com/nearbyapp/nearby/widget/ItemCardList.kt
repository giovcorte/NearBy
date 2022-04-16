package com.nearbyapp.nearby.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.databinding.annotations.BindableView
import com.databinding.databinding.IData
import com.databinding.databinding.IView
import com.nearbyapp.nearby.R

@BindableView
class ItemCardList : ConstraintLayout, IView {

    var list: RecyclerView? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.item_card_list, this)
        list = findViewById(R.id.list)
    }

    override fun name(): String {
        return "ItemCardList"
    }
}