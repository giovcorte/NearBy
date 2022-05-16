package com.nearbyapp.nearby.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.databinding.databinding.IViewAction
import com.nearbyapp.nearby.R
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
        navigationManager.updateToolbarMenu(R.menu.menu_home)
        val search: MenuItem = getToolbar().menu.findItem(R.id.search)
        val searchView: SearchView = search.actionView as SearchView
        searchView.queryHint = "Cerca..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(request: String): Boolean {
                clipboard.wrapper("near").put("query", request)
                clipboard.wrapper("near").put("fromUser", true)
                navigationManager.navigateTo("near")
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
        adapter.addItems(getHomeList())
    }

    override fun createLinearLayoutManager(): LinearLayoutManager {
        return LinearLayoutManager(context)
    }

    override fun onDestroyView() {
        getToolbar().menu.clear()
        super.onDestroyView()
    }

    private fun getHomeList(): MutableList<Identifiable> {
        return Constants.TITLES.mapIndexed { i, o ->
            HomeCategory(o, Constants.ICONS[i], object : IViewAction {
                override fun onClick() {
                    clipboard.wrapper("near").put("query", Constants.TITLES[i])
                    clipboard.wrapper("near").put("fromUser", false)
                    navigationManager.navigateTo("near")
                }
            })
        }.toMutableList()
    }

}