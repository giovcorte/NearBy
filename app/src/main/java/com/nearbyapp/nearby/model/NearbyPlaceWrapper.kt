package com.nearbyapp.nearby.model

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.nearbyapp.nearby.recycler.Identifiable
import com.nearbyapp.nearby.model.detail.Detail
import com.nearbyapp.nearby.widget.ItemNearbyPlace

@BindableObject(view = ItemNearbyPlace::class)
data class NearbyPlaceWrapper(val detail: Detail) : IData, Identifiable {
    override fun name(): String {
        return "NearbyPlaceWrapper"
    }

    override fun id(): String {
        return detail.place_id
    }

}
