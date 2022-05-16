package com.nearbyapp.nearby.model

import com.databinding.annotations.BindableObject
import com.nearbyapp.nearby.recycler.Identifiable
import com.nearbyapp.nearby.widget.ItemSpinner

@BindableObject(view = ItemSpinner::class)
data class ProgressWrapper(val indefinite: Boolean = true): Identifiable {
    override fun id(): String {
        return "progress-wrapper"
    }

    override fun name(): String {
        return "ProgressWrapper"
    }
}
