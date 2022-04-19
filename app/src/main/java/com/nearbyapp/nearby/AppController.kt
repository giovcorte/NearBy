package com.nearbyapp.nearby

import android.app.Application
import android.content.Context
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.nearbyapp.nearby.components.Clipboard
import com.nearbyapp.nearby.components.DownloadManagerHelper
import com.nearbyapp.nearby.components.PreferencesManager
import com.nearbyapp.nearby.repository.AppDatabase
import com.nearbyapp.nearby.repository.PlacesService
import com.nearbyapp.nearby.repository.PolylineService
import com.nearbyapp.nearby.repository.RepositoryImpl

class AppController: Application(), OnMapsSdkInitializedCallback {

    lateinit var repository: RepositoryImpl
    lateinit var clipboard: Clipboard
    lateinit var preferencesManager: PreferencesManager
    lateinit var downloadMnagerHelper: DownloadManagerHelper

    override fun onCreate() {
        super.onCreate()
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
        repository = RepositoryImpl(
            PlacesService.getInstance(),
            PolylineService.getInstance(),
            AppDatabase.getInstance(applicationContext).placesDAO()
        )
        clipboard = Clipboard()
        preferencesManager = PreferencesManager(this.getSharedPreferences("NearByPreferences", Context.MODE_PRIVATE))
        downloadMnagerHelper = DownloadManagerHelper(this)
    }

    override fun onMapsSdkInitialized(p0: MapsInitializer.Renderer) {

    }

}