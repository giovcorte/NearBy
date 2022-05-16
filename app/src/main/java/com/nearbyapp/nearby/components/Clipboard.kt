package com.nearbyapp.nearby.components

import com.nearbyapp.nearby.navigation.IFragmentManager

class Clipboard : IFragmentManager.IFragmentStateListener {

    private val clipboard: MutableMap<String, ClipboardWrapper> = HashMap()

    inner class ClipboardWrapper(private val values: MutableMap<String, Any?> = HashMap()) {

        fun put(key: String, data: Any) {
            values[key] = data
        }

        fun get(key: String) : Any? {
            return values[key]
        }
    }

    fun wrapper(holder: String) : ClipboardWrapper {
        return clipboard.getOrPut(holder) { ClipboardWrapper() }
    }

    override fun tagInsteadOfNameRequired(): Boolean {
        return false
    }

    override fun onFragmentMovedToBackStack(entry: String) {

    }

    override fun onFragmentRemoved(entry: String) {
        clipboard.remove(entry)
    }

}