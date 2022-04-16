package com.nearbyapp.nearby.widget

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import android.view.LayoutInflater
import com.databinding.annotations.BindableView
import com.databinding.databinding.IView
import com.nearbyapp.nearby.R

@BindableView
class ItemSpinner(context: Context) : ConstraintLayout(context), IView {

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.item_spinner, this)
    }

    override fun name(): String {
        return "ItemSpinner"
    }
}