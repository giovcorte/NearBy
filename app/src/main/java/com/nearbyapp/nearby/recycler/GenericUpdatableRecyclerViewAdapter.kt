package com.nearbyapp.nearby.recycler

import androidx.recyclerview.widget.DiffUtil
import com.databinding.databinding.IAdapterDataBinding
import com.databinding.databinding.IData
import com.databinding.databinding.IViewFactory
import com.databinding.databinding.adapter.GenericRecyclerViewAdapter

class GenericUpdatableRecyclerViewAdapter(
    dataBinding: IAdapterDataBinding,
    viewFactory: IViewFactory
) : GenericRecyclerViewAdapter(
    dataBinding,
    viewFactory
) {

    fun update(newList: List<IData>) {
        val diffResult = DiffUtil.calculateDiff(
            IdentifiableDiffUtilCallback(
            getItems().map { it as Identifiable }.toMutableList(),
            newList.map { it as Identifiable }.toMutableList())
        )
        getItems().clear()
        getItems().addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}