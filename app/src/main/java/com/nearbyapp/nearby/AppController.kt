package com.nearbyapp.nearby

import android.app.Application
import android.content.Context
import android.os.Environment
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.nearbyapp.nearby.components.*
import com.nearbyapp.nearby.loader.ImageLoader
import com.nearbyapp.nearby.loader.cache.ImageCache
import com.nearbyapp.nearby.repository.AppDatabase
import com.nearbyapp.nearby.repository.PlacesService
import com.nearbyapp.nearby.repository.PolylineService
import com.nearbyapp.nearby.repository.RepositoryImpl
import java.io.File

class AppController: Application(), OnMapsSdkInitializedCallback {

    lateinit var repository: RepositoryImpl
    lateinit var clipboard: Clipboard
    lateinit var preferencesManager: PreferencesManager
    lateinit var imageLoader: ImageLoader

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
        imageLoader = ImageLoader(this)
    }

    override fun onMapsSdkInitialized(p0: MapsInitializer.Renderer) {

    }

}