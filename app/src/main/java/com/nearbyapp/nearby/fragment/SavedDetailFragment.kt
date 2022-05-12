package com.nearbyapp.nearby.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.nearbyapp.nearby.repository.DataSource
import com.nearbyapp.nearby.viewmodel.SavedDetailViewModel

class SavedDetailFragment: ListFragment() {

    private lateinit var id: String
    private lateinit var name: String

    private lateinit var viewModel: SavedDetailViewModel

    override fun doOnCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {
        name = clipboard.getData("name") as String
        id = clipboard.getData("id") as String

        viewModel = ViewModelProvider(this)[SavedDetailViewModel::class.java]

        navigationManager.updateToolbar(name)
    }

    override fun doOnViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.details.observe(viewLifecycleOwner) {
            adapter.addItems(it)
            adapter.notifyItemRangeInserted(0, adapter.itemCount)
        }
        if (viewModel.details.value.isNullOrEmpty()) {
            viewModel.loadDetails(id, DataSource.DATABASE)
        }
    }
}