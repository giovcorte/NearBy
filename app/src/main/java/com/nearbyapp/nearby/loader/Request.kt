package com.nearbyapp.nearby.loader

import com.nearbyapp.nearby.loader.cache.ImageCache

interface Request {

    fun asString(): String

    fun cachingStrategy(): ImageCache.CachingStrategy = ImageCache.CachingStrategy.ALL

    fun cachingKey() : String = asString()

    companion object {
        fun just(s: String) : Request {
            return object : Request {
                override fun asString(): String {
                    return s
                }
            }
        }
    }

}