package com.nearbyapp.nearby.widget

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import android.widget.TextView
import android.view.LayoutInflater
import com.databinding.annotations.BindWith
import com.databinding.annotations.BindableView
import com.databinding.databinding.IView
import com.nearbyapp.nearby.R

@BindableView
class ItemDetail : ConstraintLayout, IView {

    @BindWith(paths = ["Detail.place_name:String"])
    var name: TextView
    @BindWith(paths = ["Detail.formatted_address:String"])
    var address: TextView
    @BindWith(paths = ["Detail.rating_text:String"])
    var rating: TextView
    @BindWith(paths = ["Detail.price_text:String"])
    var price: TextView

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.item_detail, this, true)
        name = findViewById(R.id.name)
        address = findViewById(R.id.address)
        rating = findViewById(R.id.rating)
        price = findViewById(R.id.price)
    }

    override fun name(): String {
        return "ItemDetail"
    }
}