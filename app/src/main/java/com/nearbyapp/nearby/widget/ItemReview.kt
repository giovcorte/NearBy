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
class ItemReview : ConstraintLayout, IView {

    @BindWith(paths = ["Review.author_name:String"])
    var author: TextView
    @BindWith(paths = ["Review.rating_text:String"])
    var rating: TextView
    @BindWith(paths = ["Review.text:String"])
    var text: TextView
    @BindWith(paths = ["Review.page:String"])
    var page: TextView

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.item_review, this, true)
        author = findViewById(R.id.author)
        rating = findViewById(R.id.rating)
        text = findViewById(R.id.text)
        page = findViewById(R.id.page)
    }

    override fun name(): String {
        return "ItemReview"
    }
}