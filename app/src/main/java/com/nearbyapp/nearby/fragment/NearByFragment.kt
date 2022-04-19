package com.nearbyapp.nearby.fragment

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.nearbyapp.nearby.components.Status
import com.nearbyapp.nearby.model.ProgressWrapper
import com.nearbyapp.nearby.model.TextWrapper
import com.nearbyapp.nearby.viewmodel.ActivityViewModel
import com.nearbyapp.nearby.viewmodel.NearByViewModel

class NearByFragment: LocalizationFragmentV2() {

    private lateinit var viewModel: NearByViewModel
    private lateinit var activityViewModel: ActivityViewModel

    private var query: String? = null

    override fun doOnCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {
        activityViewModel = ViewModelProvider(requireActivity())[ActivityViewModel::class.java]
        viewModel = ViewModelProvider(this)[NearByViewModel::class.java]
        scroll(viewModel.position)
        setScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                    val totalItemCount = linearLayoutManager.itemCount
                    val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                    val visibleThreshold = 5

                    if (totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (!loading && viewModel.token != null) {
                            loading = true
                            adapter.addItem(ProgressWrapper())
                            adapter.notifyItemInserted(adapter.itemCount - 1)
                            viewModel.loadMorePlaces()
                        }
                    }
            }
        })
        query = clipboard.getData("query") as String
        navigationManager.updateToolbar(query)
    }

    override fun doOnViewCreated(view: View, savedInstanceState: Bundle?) {
        activityViewModel.errorState.observe(viewLifecycleOwner) { status ->
            if (status == Status.READY) {
                clean()
                if (viewModel.places.value.isNullOrEmpty() && !loading) {
                    loading = true
                    loading(true)
                    localize()
                }
            } else {
                loading = false
                error(status)
            }
        }
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (activityViewModel.errorState.value == Status.READY) {
                loading(loading)
            }
        }
        viewModel.places.observe(viewLifecycleOwner) { places ->
            if ( adapter.itemCount > 0 && adapter.getItem(adapter.itemCount - 1) is ProgressWrapper) {
                adapter.removeItem(adapter.itemCount - 1)
            }
            if (places.isNotEmpty()) {
                adapter.update(places)
            } else {
                adapter.update(listOf(TextWrapper("Nessun luogo trovato")))
            }
            loading = false
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.position = position()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activityViewModel.postServiceValue(true)
    }

    override fun onLocationChanged(lastLocation: Location?) {
        lastLocation?.let {
            viewModel.lat = lastLocation.latitude
            viewModel.lng = lastLocation.longitude
            viewModel.loadPlaces(lastLocation.latitude, lastLocation.longitude, query!!.lowercase())
        }
    }

    override fun onPermissionsReady() {
        localize()
    }

    override fun onPermissionError(missingPermission: MissingPermission?) {
        error(Status.LOCATION)
    }
}