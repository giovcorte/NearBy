package com.nearbyapp.nearby.widget

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.databinding.annotations.BindWith
import com.databinding.annotations.BindableView
import com.databinding.databinding.IView
import com.nearbyapp.nearby.R

@BindableView
class ItemNearbyPlace(context: Context): ConstraintLayout(context), IView {

    //@BindWith(paths = ["NearbyPlace.thumbnail:String"])
    val image: ImageView
    @BindWith(paths = ["NearbyPlace.name:String", "NearbyPlaceWrapper.detail.place_name:String"])
    val name: TextView
    @BindWith(paths = ["NearbyPlace.vicinity:String", "NearbyPlaceWrapper.detail.formatted_address:String"])
    val address: TextView
    @BindWith(paths = ["NearbyPlace.opening_hours:OpeningHours", "NearbyPlaceWrapper.detail.opening_hours:OpeningHours"])
    val open: TextView
    @BindWith(paths = ["NearbyPlace.price:String", "NearbyPlaceWrapper.detail.priceText:String"])
    val price: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.item_nearby_place, this)
        this.image = findViewById(R.id.image)
        this.name = findViewById(R.id.name)
        this.address = findViewById(R.id.address)
        this.open = findViewById(R.id.open)
        this.price = findViewById(R.id.price)
    }

    override fun name(): String {
        return "ItemNearbyPlace"
    }
}