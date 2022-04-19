package com.nearbyapp.nearby.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nearbyapp.nearby.R
import com.nearbyapp.nearby.recycler.GenericUpdatableRecyclerViewAdapter
import com.nearbyapp.nearby.components.Status
import com.nearbyapp.nearby.widget.ErrorView

abstract class ListFragment: BaseFragment() {

    private lateinit var errorView: ErrorView
    private lateinit var progressView: ProgressBar
    private lateinit var recyclerView: RecyclerView

    lateinit var adapter: GenericUpdatableRecyclerViewAdapter
    lateinit var linearLayoutManager: LinearLayoutManager

    var loading: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_list, container, false)
        recyclerView = rootView.findViewById(R.id.list)
        progressView = rootView.findViewById(R.id.progressBar)
        errorView = rootView.findViewById(R.id.errorView)
        linearLayoutManager = LinearLayoutManager(context)
        adapter = GenericUpdatableRecyclerViewAdapter(adapterDataBinding, viewFactory)

        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        doOnCreateView(inflater, container, savedInstanceState)
        return rootView
    }

    fun setScrollListener(listener: RecyclerView.OnScrollListener) {
        recyclerView.addOnScrollListener(listener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        doOnViewCreated(view, savedInstanceState)
    }



    abstract fun doOnCreateView(inflater: LayoutInflater,
                                container: ViewGroup?,
                                savedInstanceState: Bundle?)

    abstract fun doOnViewCreated(view: View, savedInstanceState: Bundle?)

    fun loading(loading: Boolean) {
        if (loading) {
            show(progressView)
        } else {
            hide(progressView)
        }
    }

    fun error(error: Status) {
        when(error) {
            Status.INTERNET,
            Status.SERVICE,
            Status.GENERIC,
            Status.LOCATION -> {
                hide(progressView)
                hide(recyclerView)
                show(errorView)
                dataBinding.bind(errorView, error)
            }
            else -> {
                hide(errorView)
                show(recyclerView)
            }
        }
    }

    fun getRecyclerView(): RecyclerView {
        return recyclerView
    }

    fun clean() {
        hide(errorView)
        show(recyclerView)
    }

    fun position(): Int {
        return linearLayoutManager.findFirstCompletelyVisibleItemPosition()
    }

    fun scroll(position: Int) {
        linearLayoutManager.scrollToPosition(position)
    }

}