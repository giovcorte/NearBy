package com.nearbyapp.nearby.model.settings

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.nearbyapp.nearby.widget.ItemSingleTextCheckbox

@BindableObject(ItemSingleTextCheckbox::class)
data class CachePreference(val text: String, val selected: Boolean) : IData {
    override fun name(): String {
        return "CachePreference"
    }
}