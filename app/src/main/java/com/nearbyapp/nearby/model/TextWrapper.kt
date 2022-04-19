package com.nearbyapp.nearby.model

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.nearbyapp.nearby.widget.ItemText

@BindableObject(view = ItemText::class)
data class TextWrapper(val text: String) : IData {

    override fun name(): String {
        return "TextWrapper"
    }

}