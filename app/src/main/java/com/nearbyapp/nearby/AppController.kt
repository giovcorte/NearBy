package com.nearbyapp.nearby

import android.app.Application
import android.content.Context
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.nearbyapp.nearby.components.Clipboard
import com.nearbyapp.nearby.components.PreferencesManager
import com.nearbyapp.nearby.repository.PlacesService
import com.nearbyapp.nearby.repository.PolylineService
import com.nearbyapp.nearby.repository.RepositoryImpl

class AppController: Application(), OnMapsSdkInitializedCallback {

    val repository: RepositoryImpl = RepositoryImpl(PlacesService.getInstance(), PolylineService.getInstance())
    val clipboard: Clipboard = Clipboard()
    var preferencesManager: PreferencesManager? = null

    override fun onCreate() {
        super.onCreate()
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST, this)
        preferencesManager = PreferencesManager(this.getSharedPreferences("NearByPreferences", Context.MODE_PRIVATE))
    }

    override fun onMapsSdkInitialized(p0: MapsInitializer.Renderer) {

    }

}