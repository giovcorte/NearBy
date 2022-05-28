package com.nearbyapp.nearby.model

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IViewAction
import com.nearbyapp.nearby.components.StandardAction
import com.nearbyapp.nearby.recycler.Identifiable
import com.nearbyapp.nearby.recycler.Spannable
import com.nearbyapp.nearby.widget.ItemHome

@BindableObject(view = ItemHome::class)
data class HomeCategory(
    val category: String,
    val image: String,
    val action: IViewAction = object : IViewAction {
        override fun onClick() {

        }
    },
    val standardAction: StandardAction = StandardAction.NONE,
    val data: String = "",
    val span: Int = 2
): Identifiable, Spannable {

    override fun id(): String {
        return category
    }

    override fun name(): String {
        return "HomeCategory"
    }

    override fun span(): Int {
        return span
    }

}