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
import com.nearbyapp.nearby.components.Status
import com.nearbyapp.nearby.loader.ImageLoader
import com.nearbyapp.nearby.viewmodel.ActivityViewModel
import com.nearbyapp.nearby.viewmodel.DetailViewModel
import kotlin.properties.Delegates


class DetailFragment: ListFragment() {

    private lateinit var id: String
    private lateinit var name: String
    private var lat by Delegates.notNull<Double>()
    private var lng by Delegates.notNull<Double>()

    private lateinit var activityViewModel: ActivityViewModel
    private lateinit var viewModel: DetailViewModel

    private val menuListener = object : BaseActivity.MenuListener {
        override fun onItemSelected(item: MenuItem): Boolean {
            val itemId: Int = item.itemId
            if (itemId == R.id.save) {
                viewModel.saveImage()
                return true
            } else if (itemId == R.id.unsave) {
                viewModel.deleteDetails()
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
        lat = clipboard.getData("lat") as Double
        lng = clipboard.getData("lng") as Double
        navigationManager.updateToolbar(name)
        (activity as BaseActivity).registerMenuListener(menuListener)
    }

    override fun doOnViewCreated(view: View, savedInstanceState: Bundle?) {
        activityViewModel.errorState.observe(viewLifecycleOwner) { status ->
            when (status) {
                Status.READY -> {
                    clean()
                    if (viewModel.details.value.isNullOrEmpty() && !loading) {
                        loading = true
                        viewModel.loadDetails(id, lat, lng)
                    }
                }
                else -> {
                    error(status)
                }
            }
        }
        viewModel.favorite.observe(viewLifecycleOwner) { fav ->
            navigationManager.updateToolbarMenu(if (fav) R.menu.menu_saved else R.menu.menu_save)
        }
        viewModel.imagesState.observe(viewLifecycleOwner) { imageStatus ->
            when(imageStatus) {
                ImageLoader.Status.COMPLETED -> {
                    loading(false)
                    viewModel.saveDetails()
                    Toast.makeText(context, "Luogo salvato", Toast.LENGTH_SHORT).show()
                }
                ImageLoader.Status.WRITING -> {
                    loading(true)
                }
                ImageLoader.Status.ERROR -> {
                    loading(false)
                    dialog("Errore", "Impossibile scaricare le immagini")
                }
                else -> { loading(false) }
            }
        }
        viewModel.details.observe(viewLifecycleOwner) { details ->
            adapter.addItems(details)
            adapter.notifyItemRangeInserted(0, adapter.itemCount)
            loading = false
        }
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            loading(loading)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //imageCacheHelper.abortWritingImages()
    }

}