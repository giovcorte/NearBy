package com.nearbyapp.nearby.loader

import com.nearbyapp.nearby.loader.cache.ImageCache

class RequestWrapper(
    private val source: String,
    private var tag: String? = null,
    private var cache: ImageCache.CachingStrategy = ImageCache.CachingStrategy.ALL
): Request {

    override fun asString(): String {
        return source
    }

    override fun cachingStrategy(): ImageCache.CachingStrategy {
        return cache
    }

    override fun cachingKey(): String {
        return tag ?: source
    }

    fun setTag(tag: String?) {
        this.tag = tag
    }

    fun setCache(cache: ImageCache.CachingStrategy) {
        this.cache = cache
    }
}