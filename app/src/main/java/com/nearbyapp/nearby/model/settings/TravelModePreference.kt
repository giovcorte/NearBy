package com.nearbyapp.nearby.model.settings

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.nearbyapp.nearby.widget.ItemTextCheckbox

@BindableObject(view = ItemTextCheckbox::class)
data class TravelModePreference(val first: TravelMode, val second: TravelMode) : IData {
    override fun name(): String {
        return "TravelModePreference"
    }
}