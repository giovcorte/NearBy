package com.nearbyapp.nearby.model

import com.databinding.annotations.BindableObject
import com.nearbyapp.nearby.recycler.Identifiable
import com.nearbyapp.nearby.recycler.Spannable
import com.nearbyapp.nearby.widget.ItemText

@BindableObject(view = ItemText::class)
data class TextWrapper(val text: String, val matchParent: Boolean = false, val span: Int = 2) : Identifiable, Spannable {

    override fun name(): String {
        return "TextWrapper"
    }

    override fun id(): String {
        return this.hashCode().toString()
    }

    override fun span(): Int {
        return span
    }

}