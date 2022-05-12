package com.nearbyapp.nearby.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.nearbyapp.nearby.BaseActivity
import com.nearbyapp.nearby.R
import com.nearbyapp.nearby.components.ImageStorageHelper
import com.nearbyapp.nearby.components.Status
import com.nearbyapp.nearby.repository.DataSource
import com.nearbyapp.nearby.viewmodel.ActivityViewModel
import com.nearbyapp.nearby.viewmodel.DetailViewModel


class DetailFragment: ListFragment() {

    private lateinit var id: String
    private lateinit var name: String

    private lateinit var activityViewModel: ActivityViewModel
    private lateinit var viewModel: DetailViewModel

    private var dataSource: DataSource = DataSource.SERVICE

    private val menuListener = object : BaseActivity.MenuListener {
        override fun onItemSelected(item: MenuItem): Boolean {
            if (item.itemId == R.id.save) {
                viewModel.saveImage()
                return true
            } else if (item.itemId == R.id.unsave) {
                viewModel.deletePlace()
                return true
            }
            return true
        }
    }

    override fun doOnCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {
        activityViewModel = ViewModelProvider(requireActivity())[ActivityViewModel::class.java]
        viewModel = ViewModelProvider(this)[DetailViewModel::class.java]

        id = clipboard.getData("id") as String
        name = clipboard.getData("name") as String
        viewModel.lat = clipboard.getData("lat") as Double
        viewModel.lng = clipboard.getData("lng") as Double
        navigationManager.updateToolbar(name)
        (activity as BaseActivity).registerMenuListener(menuListener)
    }

    override fun doOnViewCreated(view: View, savedInstanceState: Bundle?) {
        activityViewModel.errorState.observe(viewLifecycleOwner) { status ->
            when {
                status == Status.READY || dataSource == DataSource.CACHE -> {
                    if (viewModel.details.value.isNullOrEmpty() && !loading) {
                        loadPlaceDetails()
                    }
                }
                status != Status.READY && preferencesManager.getCacheEnabled().selected -> {
                    dataSource = DataSource.CACHE
                    loadPlaceDetails()
                }
                else -> {
                    showErrorView(status, true) {
                        dataSource = DataSource.CACHE
                        loadPlaceDetails()
                    }
                }
            }
        }
        viewModel.favorite.observe(viewLifecycleOwner) { fav ->
            navigationManager.updateToolbarMenu(if (fav) R.menu.menu_saved else R.menu.menu_save)
        }
        viewModel.imagesState.observe(viewLifecycleOwner) { imageStatus ->
            when(imageStatus) {
                ImageStorageHelper.Status.COMPLETED -> {
                    showLoading(false)
                    viewModel.saveDetails()
                    Toast.makeText(context, "Luogo salvato", Toast.LENGTH_SHORT).show()
                }
                ImageStorageHelper.Status.WRITING -> {
                    showLoading(true)
                }
                ImageStorageHelper.Status.ERROR -> {
                    showLoading(false)
                    dialog("Errore", "Impossibile scaricare le immagini")
                }
                else -> { showLoading(false) }
            }
        }
        viewModel.details.observe(viewLifecycleOwner) { details ->
            adapter.update(details)
            loading = false
        }
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (activityViewModel.errorState.value == Status.READY || dataSource == DataSource.CACHE) {
                showLoading(loading)
            }
        }
    }

    private fun loadPlaceDetails() {
        cleanErrorView()
        loading = true
        viewModel.loadDetails(id, dataSource)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        imageLoader.abort()
    }

}