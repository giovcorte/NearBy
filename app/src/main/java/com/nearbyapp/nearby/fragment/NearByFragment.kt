package com.nearbyapp.nearby.fragment

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nearbyapp.nearby.components.Status
import com.nearbyapp.nearby.repository.DataSource
import com.nearbyapp.nearby.viewmodel.ActivityViewModel
import com.nearbyapp.nearby.viewmodel.NearByViewModel

class NearByFragment: LocalizationFragmentV2() {

    private lateinit var viewModel: NearByViewModel
    private lateinit var activityViewModel: ActivityViewModel

    private var fromUser: Boolean = false
    private var query: String? = null
    private var dataSource: DataSource = DataSource.SERVICE

    override fun doOnCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {
        activityViewModel = ViewModelProvider(requireActivity())[ActivityViewModel::class.java]
        viewModel = ViewModelProvider(this)[NearByViewModel::class.java]
        scrollListToPosition(viewModel.position)
        setScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                    val totalItemCount = linearLayoutManager.itemCount
                    val lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                    val visibleThreshold = 5

                    if (totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                        if (!loading && viewModel.token != null) {
                            loading = true
                            adapter.addLoader()
                            viewModel.loadMorePlaces(dataSource)
                        }
                    }
            }
        })
        query = clipboard.wrapper("near").get("query") as String
        fromUser = clipboard.wrapper("near").get("fromUser") as Boolean
        navigationManager.updateToolbar(query)
    }

    override fun doOnViewCreated(view: View, savedInstanceState: Bundle?) {
        activityViewModel.errorState.observe(viewLifecycleOwner) { status ->
            when {
                status == Status.READY || dataSource == DataSource.CACHE -> {
                    if (viewModel.placesData.value.isNullOrEmpty() && !loading) {
                        loadPlaces()
                    }
                }
                status != Status.READY && preferencesManager.getCacheEnabled().selected -> {
                    dataSource = DataSource.CACHE
                    loadPlaces()
                }
                else -> {
                    loading = false
                    showLoadingView(false)
                    showErrorView(status, true) {
                        dataSource = DataSource.CACHE
                        loadPlaces()
                    }
                }
            }
        }
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (activityViewModel.errorState.value == Status.READY || dataSource == DataSource.CACHE) {
                showLoadingView(loading)
            }
        }
        viewModel.placesData.observe(viewLifecycleOwner) { places ->
            adapter.update(places)
            loading = false
        }
    }

    override fun createLinearLayoutManager(): LinearLayoutManager {
        return LinearLayoutManager(context)
    }

    private fun loadPlaces() {
        cleanErrorView()
        loading = true
        showLoadingView(true)
        when (dataSource) {
            DataSource.CACHE -> {
                viewModel.loadPlaces(viewModel.userLatitude, viewModel.userLongitude, query!!, DataSource.CACHE)
            }
            DataSource.SERVICE -> {
                localize()
            }
            else -> {}
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.position = getCurrentListPosition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activityViewModel.postServiceValue(true)
    }

    override fun onLocationChanged(lastLocation: Location?) {
        lastLocation?.let {
            viewModel.userLatitude = lastLocation.latitude
            viewModel.userLongitude = lastLocation.longitude
            viewModel.loadPlaces(lastLocation.latitude, lastLocation.longitude, query!!.lowercase(), dataSource)
        }
    }

    override fun onPermissionsReady() {
        localize()
    }

    override fun onPermissionError(missingPermission: MissingPermission?) {
        showErrorView(Status.LOCATION, true) {
            dataSource = DataSource.CACHE
            loadPlaces()
        }
    }
}