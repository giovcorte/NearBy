package com.nearbyapp.nearby.model

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.nearbyapp.nearby.recycler.Identifiable
import com.nearbyapp.nearby.widget.ItemCardList

@BindableObject(ItemCardList::class)
data class ListWrapper(val list: MutableList<IData>, val horizontal: Boolean) : IData, Identifiable {
    override fun name(): String {
        return "ListWrapper"
    }

    override fun id(): String {
        return list.hashCode().toString()
    }

}