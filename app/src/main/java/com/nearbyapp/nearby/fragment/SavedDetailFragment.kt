package com.nearbyapp.nearby.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.nearbyapp.nearby.model.HomeCategory
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
        name = clipboard.wrapper("savedDetails").get("name") as String
        id = clipboard.wrapper("savedDetails").get("id") as String

        viewModel = ViewModelProvider(this)[SavedDetailViewModel::class.java]

        navigationManager.updateToolbar(name)
    }

    override fun doOnViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.details.observe(viewLifecycleOwner) {
            adapter.update(it)
        }
        if (viewModel.details.value.isNullOrEmpty()) {
            viewModel.loadDetails(id, DataSource.DATABASE)
        }
    }

    override fun createLinearLayoutManager(): LinearLayoutManager {
        val layoutManager = GridLayoutManager(context, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val item = adapter.getItem(position)
                return if (item is HomeCategory) 1 else 2
            }
        }
        return layoutManager
    }
}