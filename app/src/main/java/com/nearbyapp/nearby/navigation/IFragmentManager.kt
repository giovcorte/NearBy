package com.nearbyapp.nearby.navigation

import android.os.Bundle

interface IFragmentManager {

    fun pushFirstFragment(name: String)
    fun pushFragment(name: String)
    fun pushOverFragment(name: String)
    fun popOverFragment()
    fun popLastFragment()
    fun popToName(name: String)
    fun popToTag(tag: String)
    fun popToFirstFragment()

    fun isCurrentFragmentFirst(): Boolean

    fun getCurrentFragmentName(): String

    fun onSaveInstanceState(outState: Bundle)
    fun onRestoreInstanceState(inState: Bundle?)

    fun subscribeFragmentStateListener(listener: IFragmentStateListener)

    interface IFragmentStateListener {
        fun tagInsteadOfNameRequired(): Boolean
        fun onFragmentMovedToBackStack(entry: String)
        fun onFragmentRemoved(entry: String)
    }

}