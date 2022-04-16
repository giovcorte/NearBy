package com.nearbyapp.nearby.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.databinding.annotations.BindableView
import com.databinding.databinding.IView
import com.nearbyapp.nearby.R

@BindableView
class ItemMap: ConstraintLayout, IView {

    var mapView: NestedScrollingMapView

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        LayoutInflater.from(context).inflate(R.layout.item_map, this, true)
        mapView = findViewById(R.id.mapView)
    }

    override fun name(): String {
        return "ItemMap"
    }
}