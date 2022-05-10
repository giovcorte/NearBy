package com.nearbyapp.nearby.fragment

import androidx.fragment.app.Fragment

class FragmentFactory {

    companion object {
        fun createFragment(name: String): Fragment {
            when (name) {
                "home" -> return HomeFragment()
                "near" -> return NearByFragment()
                "detail" -> return DetailFragment()
                "settings" -> return SettingsFragment()
                "saved" -> return SavedPlacesFragment()
                "savedDetail" -> return SavedDetailFragment()
            }
            throw IllegalStateException()
        }
    }
}