package com.nearbyapp.nearby.model

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.google.android.gms.maps.model.PolylineOptions
import com.nearbyapp.nearby.recycler.Identifiable
import com.nearbyapp.nearby.widget.ItemMap

@BindableObject(view = ItemMap::class)
data class MapWrapper (
    val lat: Double,
    val lng: Double,
    val userLat: Double,
    val userLng: Double,
    val polylineOptions: PolylineOptions?,
    val travelMode: String
): IData, Identifiable {
    override fun name(): String {
        return "MapWrapper"
    }

    override fun id(): String {
        return "$lat,$lng,$userLat,$userLng"
    }
}