package com.nearbyapp.nearby.model

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.nearbyapp.nearby.recycler.Identifiable
import com.nearbyapp.nearby.widget.ItemText
import kotlin.random.Random

@BindableObject(view = ItemText::class)
data class TextWrapper(val text: String) : IData, Identifiable {

    override fun name(): String {
        return "TextWrapper"
    }

    override fun id(): String {
        return Random(1).toString()
    }

}