package com.nearbyapp.nearby.model

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.nearbyapp.nearby.widget.ItemSpinner

@BindableObject(view = ItemSpinner::class)
data class ProgressWrapper(val indefinite: Boolean = true): IData {
    override fun name(): String {
        return "ProgressWrapper"
    }
}
