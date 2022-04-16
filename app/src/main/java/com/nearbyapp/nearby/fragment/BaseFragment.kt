package com.nearbyapp.nearby.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.databinding.databinding.AdapterDataBinding
import com.databinding.databinding.DataBinding
import com.databinding.databinding.IViewFactory
import com.databinding.databinding.adapter.GenericRecyclerViewAdapter
import com.databinding.databinding.factory.ViewFactory
import com.nearbyapp.nearby.MainActivity
import com.nearbyapp.nearby.R
import com.nearbyapp.nearby.components.Clipboard
import com.nearbyapp.nearby.components.PreferencesManager
import com.nearbyapp.nearby.navigation.NavigationManager

abstract class BaseFragment: Fragment() {

    lateinit var navigationManager: NavigationManager
    lateinit var clipboard: Clipboard
    lateinit var preferencesManager: PreferencesManager

    lateinit var dataBinding: DataBinding
    lateinit var adapterDataBinding: AdapterDataBinding
    lateinit var viewFactory: ViewFactory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            navigationManager = context.navigationManager
            clipboard = context.clipboard
            preferencesManager = context.preferencesManager
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.dataBinding = DataBinding(preferencesManager, clipboard, navigationManager)
        this.adapterDataBinding = AdapterDataBinding(dataBinding)
        this.viewFactory = ViewFactory(requireContext())
    }

    fun dialog(title: String, message: String) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(title)
        builder.setMessage(message)
            .setPositiveButton(
                "OK"
            ) { dialog, _ ->
                dialog.dismiss()
                navigationManager.backToPrevious()
            }
        builder.create().show()
    }

    fun hide(view: View) {
        view.visibility = View.GONE
    }

    fun show(view: View) {
        view.visibility = View.VISIBLE
    }

}