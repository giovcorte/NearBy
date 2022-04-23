package com.nearbyapp.nearby.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.databinding.databinding.AdapterDataBinding
import com.databinding.databinding.DataBinding
import com.databinding.databinding.factory.ViewFactory
import com.nearbyapp.nearby.BaseActivity
import com.nearbyapp.nearby.components.Clipboard
import com.nearbyapp.nearby.components.ImageStorageHelper
import com.nearbyapp.nearby.components.PreferencesManager
import com.nearbyapp.nearby.loader.ImageLoader
import com.nearbyapp.nearby.navigation.NavigationManager

abstract class BaseFragment: Fragment() {

    lateinit var navigationManager: NavigationManager
    lateinit var clipboard: Clipboard
    lateinit var preferencesManager: PreferencesManager
    lateinit var imageLoader: ImageLoader
    lateinit var imageStorageHelper: ImageStorageHelper

    lateinit var dataBinding: DataBinding
    lateinit var adapterDataBinding: AdapterDataBinding
    lateinit var viewFactory: ViewFactory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity) {
            navigationManager = context.navigationManager
            clipboard = context.clipboard
            preferencesManager = context.preferencesManager
            imageLoader = context.imageLoader
            imageStorageHelper = context.imageStorageHelper
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.dataBinding = DataBinding(imageStorageHelper, preferencesManager, clipboard, navigationManager)
        this.adapterDataBinding = AdapterDataBinding(dataBinding)
        this.viewFactory = ViewFactory(requireContext())
    }

    fun hide(view: View) {
        view.visibility = View.GONE
    }

    fun show(view: View) {
        view.visibility = View.VISIBLE
    }

    fun dialog(title: String, message: String) {
        (requireActivity() as BaseActivity).dialog(title,message)
    }

}