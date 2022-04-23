package com.nearbyapp.nearby.loader

import android.app.Application
import java.io.File

class RequestBuilder(val application: Application) {

    fun create(source: String): Request {
        return object : Request {
            override fun source(): String {
                return source
            }
        }
    }

    fun create(source: Int): Request {
        return object : Request {
            override fun source(): String {
                return source.toString()
            }
        }
    }

    fun create(source: File): Request {
        return object : Request {
            override fun source(): String {
                return source.absolutePath
            }
        }
    }
}