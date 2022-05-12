package com.nearbyapp.nearby

import android.app.Application
import android.content.Context
import android.os.Environment
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.nearbyapp.nearby.components.Clipboard
import com.nearbyapp.nearby.components.ImageStorageHelper
import com.nearbyapp.nearby.components.PreferencesManager
import com.nearbyapp.nearby.loader.ImageLoader
import com.nearbyapp.nearby.loader.cache.diskcache.DiskCache
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
    lateinit var imageStorageHelper: ImageStorageHelper
    lateinit var diskCache: DiskCache

    override fun onCreate() {
        super.onCreate()
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
        clipboard = Clipboard()
        preferencesManager = PreferencesManager(this.getSharedPreferences("NearByPreferences", Context.MODE_PRIVATE))
        imageLoader = ImageLoader(this)
        imageStorageHelper = ImageStorageHelper(getImageFolder(), imageLoader.cache())
        diskCache = DiskCache(File(this.cacheDir.path + File.separator + "appcache"), 1024 * 1024 * 200, 1)
        repository = RepositoryImpl(
            PlacesService.getInstance(),
            PolylineService.getInstance(),
            AppDatabase.getInstance(applicationContext).placesDAO(),
            diskCache
        )
    }

    override fun onMapsSdkInitialized(p0: MapsInitializer.Renderer) {

    }

    private fun getImageFolder(): File {
        return File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath)
    }

}