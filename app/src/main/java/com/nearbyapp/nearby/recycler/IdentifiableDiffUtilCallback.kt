package com.nearbyapp.nearby.recycler

import androidx.recyclerview.widget.DiffUtil

class IdentifiableDiffUtilCallback(
    private val oldList: MutableList<Identifiable>,
    private val newList: MutableList<Identifiable>
) : DiffUtil.Callback() {


    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id() == newList[newItemPosition].id()
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}