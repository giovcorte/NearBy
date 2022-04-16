package com.nearbyapp.nearby.model

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.nearbyapp.nearby.widget.ItemTextSeekBar

@BindableObject(view = ItemTextSeekBar::class)
data class RadiusPreference(val min: Int, val current: Int, val max: Int): IData {
    override fun name(): String {
        return "RadiusPreference"
    }

    val minText: String
        get() {
            return "$min km"
        }

    val maxText: String
        get() {
            return "$max km"
        }

    val currentText: String
        get() {
            return "$current km"
        }

    fun getRadiusInKm(): Int {
        return current * 1000
    }
}