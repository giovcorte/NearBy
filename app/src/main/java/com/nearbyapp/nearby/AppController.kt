package com.nearbyapp.nearby

import android.app.Application
import android.content.Context
import android.os.Environment
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.nearbyapp.nearby.components.*
import com.nearbyapp.nearby.repository.AppDatabase
import com.nearbyapp.nearby.repository.PlacesService
import com.nearbyapp.nearby.repository.PolylineService
import com.nearbyapp.nearby.repository.RepositoryImpl
import java.io.File

class AppController: Application(), OnMapsSdkInitializedCallback {

    lateinit var repository: RepositoryImpl
    lateinit var clipboard: Clipboard
    lateinit var preferencesManager: PreferencesManager
    lateinit var imageCache: ImageCache
    lateinit var imageCacheHelper: ImageCacheHelper

    override fun onCreate() {
        super.onCreate()
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
        imageCache = ImageCache(File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath))
        repository = RepositoryImpl(
            PlacesService.getInstance(),
            PolylineService.getInstance(),
            AppDatabase.getInstance(applicationContext).placesDAO()
        )
        clipboard = Clipboard()
        preferencesManager = PreferencesManager(this.getSharedPreferences("NearByPreferences", Context.MODE_PRIVATE))
        imageCacheHelper = ImageCacheHelper(imageCache)
    }

    override fun onMapsSdkInitialized(p0: MapsInitializer.Renderer) {

    }

}