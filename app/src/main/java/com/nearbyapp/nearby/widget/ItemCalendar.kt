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
class ItemCalendar : ConstraintLayout, IView {

    var title: TextView
    @BindWith(paths = ["OpeningHours.calendar:String"])
    var calendar: TextView

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.item_calendar, this, true)
        title = findViewById(R.id.title)
        calendar = findViewById(R.id.calendar)
    }

    override fun name(): String {
        return "ItemCalendar"
    }

}