package com.nearbyapp.nearby.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.databinding.databinding.IViewAction
import com.nearbyapp.nearby.model.Constants
import com.nearbyapp.nearby.model.HomeCategory
import com.nearbyapp.nearby.recycler.Identifiable

class HomeFragment: ListFragment() {

    override fun doOnCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {
        navigationManager.updateToolbar("Home")
    }

    override fun doOnViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter.addItems(getHomeList())
    }

    private fun getHomeList(): MutableList<Identifiable> {
        return Constants.TITLES.mapIndexed { i, o ->
            HomeCategory(o, Constants.ICONS[i], object : IViewAction {
                override fun onClick() {
                    clipboard.putData("query", Constants.TITLES[i])
                    navigationManager.navigateTo("near")
                }
            })
        }.toMutableList()
    }

}