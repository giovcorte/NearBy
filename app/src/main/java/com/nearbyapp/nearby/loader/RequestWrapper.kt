package com.nearbyapp.nearby.loader

import com.nearbyapp.nearby.loader.cache.IImageCache

class RequestWrapper(
    private val source: String,
    private var tag: String? = null,
    private var cache: IImageCache.CachingStrategy
): Request {

    override fun asString(): String {
        return source
    }

    override fun cachingStrategy(): IImageCache.CachingStrategy {
        return cache
    }

    override fun cachingKey(): String {
        return tag ?: source
    }

}