package com.nearbyapp.nearby

import android.content.IntentFilter
import android.location.LocationManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import com.nearbyapp.nearby.components.Clipboard
import com.nearbyapp.nearby.components.GPSReceiver
import com.nearbyapp.nearby.components.PreferencesManager
import com.nearbyapp.nearby.navigation.FragmentManagerHelper
import com.nearbyapp.nearby.navigation.NavigationManager
import com.nearbyapp.nearby.viewmodel.ActivityViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var fragmentManagerHelper: FragmentManagerHelper
    lateinit var navigationManager: NavigationManager
    lateinit var clipboard: Clipboard
    lateinit var preferencesManager: PreferencesManager

    private lateinit var drawer: DrawerLayout
    private lateinit var toolbar: Toolbar

    private lateinit var viewModel: ActivityViewModel

    private val locationSensorCallBack = object : GPSReceiver.LocationSensorCallBack {
        override fun enabled() {
            viewModel.postLocationValue(true)
        }

        override fun disabled() {
            viewModel.postLocationValue(false)
        }

    }

    private val gpsReceiver = GPSReceiver(locationSensorCallBack)

    override fun onCreate(savedInstanceState: Bundle?) {
        fragmentManagerHelper = FragmentManagerHelper(R.id.host, supportFragmentManager)
        navigationManager = NavigationManager(this, fragmentManagerHelper)
        clipboard = (application as AppController).clipboard
        preferencesManager = (application as AppController).preferencesManager!!

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[ActivityViewModel::class.java]

        toolbar = findViewById(R.id.toolbar)
        drawer = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)

        navigationView.setNavigationItemSelectedListener { menuItem: MenuItem? ->
            selectDrawerItem(menuItem)
            true
        }

        navigationManager.initNavigation("home")
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(gpsReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        registerReceiver(gpsReceiver, IntentFilter(LocationManager.MODE_CHANGED_ACTION))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(gpsReceiver)
    }

    override fun onBackPressed() {
        navigationManager.backToPrevious()
    }

    fun updateToolbar(isStart: Boolean, title: String?) {
        toolbar.title = title
        if (!isStart) {
            toolbar.setNavigationIcon(R.drawable.arrow_back)
            toolbar.setNavigationOnClickListener { navigationManager.backToPrevious() }
        } else {
            toolbar.setNavigationIcon(R.drawable.menu)
            toolbar.setNavigationOnClickListener { drawer.openDrawer(GravityCompat.START) }
        }
    }

    private fun selectDrawerItem(menuItem: MenuItem?) {
        val itemId = menuItem?.itemId
        if (itemId == R.id.settings) {
            menuItem.isChecked = false
            navigationManager.navigateTo("settings")
            drawer.closeDrawers()
        }
    }

}