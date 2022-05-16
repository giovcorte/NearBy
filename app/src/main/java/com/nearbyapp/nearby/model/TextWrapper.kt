package com.nearbyapp.nearby.model

import com.databinding.annotations.BindableObject
import com.nearbyapp.nearby.recycler.Identifiable
import com.nearbyapp.nearby.widget.ItemText

@BindableObject(view = ItemText::class)
data class TextWrapper(val text: String, val matchParent: Boolean = false) : Identifiable {

    override fun name(): String {
        return "TextWrapper"
    }

    override fun id(): String {
        return this.hashCode().toString()
    }

}