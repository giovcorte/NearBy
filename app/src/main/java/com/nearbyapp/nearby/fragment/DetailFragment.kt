package com.nearbyapp.nearby.fragment

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.nearbyapp.nearby.R
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
        setHasOptionsMenu(true)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_detail, menu)
        val submenu: Menu = menu.getItem(0).subMenu
        for (i in 0 until submenu.size()) {
            val item: MenuItem = submenu.getItem(i)
            val title = SpannableString(submenu.getItem(i).title.toString())
            title.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.mediumgray
                    )
                ), 0, title.length, 0
            )
            item.title = title
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId: Int = item.itemId
        if (itemId == R.id.save) {
            viewModel.saveDetails()
            submitDownload(viewModel.saveImage())
            return true
        }
        return super.onOptionsItemSelected(item)
    }


}