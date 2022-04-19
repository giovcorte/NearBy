package com.nearbyapp.nearby

import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import com.nearbyapp.nearby.components.Clipboard
import com.nearbyapp.nearby.components.ImageCacheHelper
import com.nearbyapp.nearby.components.PreferencesManager
import com.nearbyapp.nearby.navigation.FragmentManagerHelper
import com.nearbyapp.nearby.navigation.NavigationManager
import com.nearbyapp.nearby.viewmodel.ActivityViewModel


class BaseActivity : AppCompatActivity() {

    private lateinit var fragmentManagerHelper: FragmentManagerHelper
    lateinit var navigationManager: NavigationManager
    lateinit var clipboard: Clipboard
    lateinit var preferencesManager: PreferencesManager
    lateinit var imageCacheHelper: ImageCacheHelper

    private lateinit var drawer: DrawerLayout
    private lateinit var toolbar: Toolbar

    private lateinit var viewModel: ActivityViewModel

    interface MenuListener {
        fun onItemSelected(item: MenuItem): Boolean
    }

    private var menuListener: MenuListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        fragmentManagerHelper = FragmentManagerHelper(R.id.host, supportFragmentManager)
        navigationManager = NavigationManager(this, fragmentManagerHelper)
        clipboard = (application as AppController).clipboard
        preferencesManager = (application as AppController).preferencesManager
        imageCacheHelper = (application as AppController).imageCacheHelper

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
        setSupportActionBar(toolbar)
        navigationManager.initNavigation("home")
    }

    override fun onBackPressed() {
        navigationManager.backToPrevious()
    }

    private fun selectDrawerItem(menuItem: MenuItem?) {
        val itemId = menuItem?.itemId
        if (itemId == R.id.settings) {
            menuItem.isChecked = false
            navigationManager.navigateTo("settings")
            drawer.closeDrawers()
        } else if (itemId == R.id.saved) {
            menuItem.isChecked = false
            navigationManager.navigateTo("saved")
            drawer.closeDrawers()
        }
    }

    fun dialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
            .setPositiveButton(
                "OK"
            ) { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    fun getToolbar(): Toolbar {
        return toolbar
    }

    fun getDrawer(): DrawerLayout {
        return drawer
    }

    fun registerMenuListener(listener: MenuListener) {
        this.menuListener = listener
    }

    fun unregisterMenuListener() {
        this.menuListener = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        menuListener?.let {
            return it.onItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

}