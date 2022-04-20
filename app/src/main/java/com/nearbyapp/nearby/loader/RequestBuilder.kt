package com.nearbyapp.nearby.loader

import android.app.Application
import android.content.Context
import com.nearbyapp.nearby.loader.cache.ImageCache
import java.io.File

class RequestBuilder(val application: Application) {

    fun create(target: Target, source: String): Request {
        return object : Request {
            override fun source(): String {
                return source
            }

            override fun target(): Target {
                return target
            }

            override fun context(): Context {
                return application.applicationContext
            }

            override fun caching(): ImageCache.CachingStrategy {
                return ImageCache.CachingStrategy.ALL
            }

        }
    }

    fun create(target: Target, source: Int): Request {
        return object : Request {
            override fun source(): String {
                return source.toString()
            }

            override fun target(): Target {
                return target
            }

            override fun context(): Context {
                return application.applicationContext
            }

            override fun caching(): ImageCache.CachingStrategy {
                return ImageCache.CachingStrategy.NONE
            }

        }
    }

    fun create(target: Target, source: File): Request {
        return object : Request {
            override fun source(): String {
                return source.absolutePath
            }

            override fun target(): Target {
                return target
            }

            override fun context(): Context {
                return application.applicationContext
            }

            override fun caching(): ImageCache.CachingStrategy {
                return ImageCache.CachingStrategy.NONE
            }

        }
    }
}