package com.nearbyapp.nearby.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.nearbyapp.nearby.R
import com.nearbyapp.nearby.viewmodel.SettingsViewModel
import com.nearbyapp.nearby.widget.ItemTextCheckbox
import com.nearbyapp.nearby.widget.ItemTextSeekBar

class SettingsFragment: BaseFragment() {

    private lateinit var viewModel: SettingsViewModel

    private lateinit var radius: ItemTextSeekBar
    private lateinit var travelMode: ItemTextCheckbox
    private lateinit var travelTitle: TextView
    private lateinit var radiusTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        radius = rootView.findViewById(R.id.radius)
        radiusTitle = rootView.findViewById(R.id.radiusTitle)
        travelMode = rootView.findViewById(R.id.travelMode)
        travelTitle = rootView.findViewById(R.id.travelTitle)

        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        navigationManager.updateToolbar("Impostazioni")

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.bind(travelTitle, "Percorrenza")
        dataBinding.bind(radiusTitle, "Raggio di ricerca")
        viewModel.radiusPreference.observe(viewLifecycleOwner) {
            dataBinding.bind(radius, it)
        }
        viewModel.travelModePreference.observe(viewLifecycleOwner) {
            dataBinding.bind(travelMode, it)
        }
        viewModel.loadPreferences()
    }
}