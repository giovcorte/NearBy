package com.nearbyapp.nearby.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.nearbyapp.nearby.components.Status
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
    }

    override fun doOnViewCreated(view: View, savedInstanceState: Bundle?) {
        activityViewModel.errorState.observe(viewLifecycleOwner) { status ->
            if (status == Status.READY) {
                clean()
                if (viewModel.details.value.isNullOrEmpty() && !loading) {
                    loading = true
                    viewModel.loadDetails(id, lat, lng)
                }
            } else {
                error(status)
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


}