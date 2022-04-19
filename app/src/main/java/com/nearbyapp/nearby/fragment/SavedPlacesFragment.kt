package com.nearbyapp.nearby.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.nearbyapp.nearby.model.NearbyPlaceWrapper
import com.nearbyapp.nearby.recycler.SwipeToDeleteCallback
import com.nearbyapp.nearby.viewmodel.SavedPlacesViewModel


class SavedPlacesFragment: ListFragment() {

    private lateinit var viewModel: SavedPlacesViewModel

    override fun doOnCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {
        viewModel = ViewModelProvider(this)[SavedPlacesViewModel::class.java]
        navigationManager.updateToolbar("Luoghi salvati")
    }

    override fun doOnViewCreated(view: View, savedInstanceState: Bundle?) {
        enableSwipeToDelete()
        viewModel.places.observe(viewLifecycleOwner) {
            adapter.update(it.toMutableList())
        }
        viewModel.loadSavedPlaces()
    }

    private fun enableSwipeToDelete() {
        val swipeToDeleteCallback: SwipeToDeleteCallback =
            object : SwipeToDeleteCallback(requireContext()) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                    viewModel.deletePlace((adapter.getItem(viewHolder.adapterPosition) as NearbyPlaceWrapper).detail.place_id)
                    viewModel.loadSavedPlaces()
                    makeText(context, "Eliminazione effettuata", LENGTH_SHORT).show()
                }
            }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(getRecyclerView())
    }
}