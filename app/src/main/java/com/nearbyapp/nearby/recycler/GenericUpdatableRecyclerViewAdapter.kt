package com.nearbyapp.nearby.recycler

import androidx.recyclerview.widget.DiffUtil
import com.databinding.databinding.IAdapterDataBinding
import com.databinding.databinding.IViewFactory
import com.databinding.databinding.adapter.GenericRecyclerViewAdapter
import com.nearbyapp.nearby.model.ProgressWrapper

class GenericUpdatableRecyclerViewAdapter(
    dataBinding: IAdapterDataBinding,
    viewFactory: IViewFactory
) : GenericRecyclerViewAdapter<Identifiable>(
    dataBinding,
    viewFactory
) {

    @Synchronized
    fun update(newList: List<Identifiable>) {
        val diffResult = DiffUtil.calculateDiff(
            IdentifiableDiffUtilCallback(
            getItems(),
            newList)
        )
        getItems().clear()
        getItems().addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun addLoader() {
        addItem(ProgressWrapper())
        notifyItemInserted(itemCount - 1)
    }

    @Synchronized
    fun removeLoader() {
        getItems().forEachIndexed { index, identifiable ->
            if (identifiable is ProgressWrapper) {
                getItems().removeAt(index)
                notifyItemRemoved(index)
            }
        }
    }
}