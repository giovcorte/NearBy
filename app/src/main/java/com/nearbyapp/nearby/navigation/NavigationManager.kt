package com.nearbyapp.nearby.navigation

import android.app.Activity
import androidx.core.view.GravityCompat
import com.nearbyapp.nearby.BaseActivity
import com.nearbyapp.nearby.R

class NavigationManager(private val activity: BaseActivity, private val fragmentManager: IFragmentManager) {

    fun initNavigation(fragment: String) {
        fragmentManager.pushFirstFragment(fragment)
    }

    fun navigateTo(fragment: String) {
        fragmentManager.pushFragment(fragment)
    }

    fun backTo(name: String) {
        activity.getToolbar().menu.clear()
        fragmentManager.popToName(name)
    }

    fun backToPrevious() {
        if (fragmentManager.isCurrentFragmentFirst()) {
            activity.finish()
        } else {
            activity.getToolbar().menu.clear()
            fragmentManager.popLastFragment()
        }
    }

    fun backToStart() {
        fragmentManager.popToFirstFragment()
    }

    fun updateToolbar(title: String?) {
        activity.supportActionBar?.title = title
        when {
            !fragmentManager.isCurrentFragmentFirst() -> {
                activity.getToolbar().setNavigationIcon(R.drawable.arrow_back)
                activity.getToolbar().setNavigationOnClickListener { backToPrevious() }
            }
            else -> {
                activity.getToolbar().setNavigationIcon(R.drawable.menu)
                activity.getToolbar().setNavigationOnClickListener { activity.getDrawer().openDrawer(GravityCompat.START) }
            }
        }
    }

    fun updateToolbarMenu(resId: Int) {
        activity.getToolbar().menu.clear()
        activity.menuInflater.inflate(resId, activity.getToolbar().menu)
    }

    fun getActivityContext() : Activity {
        return activity
    }

}