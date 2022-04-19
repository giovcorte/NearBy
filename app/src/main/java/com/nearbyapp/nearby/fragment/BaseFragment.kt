package com.nearbyapp.nearby.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.databinding.databinding.AdapterDataBinding
import com.databinding.databinding.DataBinding
import com.databinding.databinding.factory.ViewFactory
import com.nearbyapp.nearby.MainActivity
import com.nearbyapp.nearby.components.Clipboard
import com.nearbyapp.nearby.components.DownloadManagerHelper
import com.nearbyapp.nearby.components.PreferencesManager
import com.nearbyapp.nearby.navigation.NavigationManager

abstract class BaseFragment: Fragment() {

    lateinit var navigationManager: NavigationManager
    lateinit var clipboard: Clipboard
    lateinit var preferencesManager: PreferencesManager
    lateinit var downloadManagerHelper: DownloadManagerHelper

    lateinit var dataBinding: DataBinding
    lateinit var adapterDataBinding: AdapterDataBinding
    lateinit var viewFactory: ViewFactory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            navigationManager = context.navigationManager
            clipboard = context.clipboard
            preferencesManager = context.preferencesManager
            downloadManagerHelper = context.downloadManagerHelper
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.dataBinding = DataBinding(preferencesManager, clipboard, downloadManagerHelper, navigationManager)
        this.adapterDataBinding = AdapterDataBinding(dataBinding)
        this.viewFactory = ViewFactory(requireContext())
    }

    fun hide(view: View) {
        view.visibility = View.GONE
    }

    fun show(view: View) {
        view.visibility = View.VISIBLE
    }

    fun submitDownload(ref: DownloadManagerHelper.DownloadRef) {
        if (ref.status == DownloadManagerHelper.DownloadRef.VALID) {
            (requireActivity() as MainActivity).submitDownloadHandler(ref)
        } else {
            (requireActivity() as MainActivity).dialog("Errore","Impossibile effetturare il download")
        }
    }

}