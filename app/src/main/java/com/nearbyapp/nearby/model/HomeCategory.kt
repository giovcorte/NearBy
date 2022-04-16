package com.nearbyapp.nearby.model

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.databinding.databinding.IViewAction
import com.nearbyapp.nearby.widget.ItemHome

@BindableObject(view = ItemHome::class)
data class HomeCategory(val category: String, val image: String, val action: IViewAction): IData {

    override fun name(): String {
        return "HomeCategory"
    }

}