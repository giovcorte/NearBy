package com.nearbyapp.nearby.navigation

import com.nearbyapp.nearby.MainActivity

class NavigationManager(var activity: MainActivity, var fragmentManager: IFragmentManager) {

    fun initNavigation(fragment: String) {
        fragmentManager.pushFirstFragment(fragment)
    }

    fun navigateTo(fragment: String) {
        fragmentManager.pushFragment(fragment)
    }

    fun backTo(name: String) {
        fragmentManager.popToName(name)
    }

    fun backToPrevious() {
        if (fragmentManager.isCurrentFragmentFirst()) {
            activity.finish()
        } else {
            fragmentManager.popLastFragment()
        }
    }

    fun backToStart() {
        fragmentManager.popToFirstFragment()
    }

    fun updateToolbar(title: String?) {
        activity.updateToolbar(fragmentManager.isCurrentFragmentFirst(), title)
    }

}