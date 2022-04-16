package com.nearbyapp.nearby.components

class Clipboard {

    private val clipboard: MutableMap<String, Any> = HashMap()

    fun putData(key: String, data: Any) {
        clipboard[key] = data
    }

    fun getData(key: String): Any? {
        return clipboard[key]
    }

}