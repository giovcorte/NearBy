package com.nearbyapp.nearby.loader

import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.nearbyapp.nearby.loader.cache.ImageCache
import com.nearbyapp.nearby.loader.fetcher.Fetcher
import com.nearbyapp.nearby.loader.fetcher.FileFetcher
import com.nearbyapp.nearby.loader.fetcher.ResourceFetcher
import com.nearbyapp.nearby.loader.fetcher.URLFetcher
import kotlinx.coroutines.*
import java.io.File

class ImageLoader(val application: Application) {

    private val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)

    companion object {
        lateinit var INSTANCE: ImageLoader

        fun get(): Builder {
            return Builder(INSTANCE)
        }
    }

    private val imageCache: ImageCache = ImageCache(application)

    private val urlFetcher: URLFetcher = URLFetcher()
    private val fileFetcher: FileFetcher = FileFetcher()
    private val resourceFetcher: ResourceFetcher = ResourceFetcher(application)

    private val requestBuilder: RequestBuilder = RequestBuilder(application)
    private val targetBuilder: TargetBuilder = TargetBuilder()

    init {
        INSTANCE = this
    }

    class Builder(val loader: ImageLoader) {
        private var request: Request? = null
        private var target: Target? = null
        private var fetcher: Fetcher? = null
        private var cache: ImageCache.CachingStrategy = ImageCache.CachingStrategy.ALL
        private var tag: String? = null

        fun into(target: Target) = apply { this.target = target }

        fun into(view: ImageView?) = apply { this.target = loader.targetBuilder.create(view)}

        fun into(view: ImageView?, placeHolder: Drawable) = apply {
            this.target = loader.targetBuilder.create(view, placeHolder)
        }

        fun load(url: String) = apply {
            this.request = loader.requestBuilder.create(url)
            this.fetcher = loader.urlFetcher
            this.cache = ImageCache.CachingStrategy.ALL
        }

        fun load(file: File) = apply {
            this.request = loader.requestBuilder.create(file)
            this.fetcher = loader.fileFetcher
            this.cache = ImageCache.CachingStrategy.NONE
        }

        fun load(res: Int) = apply {
            this.request = loader.requestBuilder.create(res)
            this.fetcher = loader.resourceFetcher
            this.cache = ImageCache.CachingStrategy.NONE
        }

        fun cache(cache: ImageCache.CachingStrategy) = apply {
            this.cache = cache
        }

        fun tag(tag: String) = apply {
            this.tag = tag
        }

        fun run() {
            safe(request, target, fetcher) { request, target, fetcher ->
                loader.load(request, target, fetcher, cache, tag)
            }
        }

        private inline fun <T1: Any, T2: Any, T3: Any, R: Any> safe(p1: T1?, p2: T2?, p3: T3?, block: (T1, T2, T3) -> R?): R? {
            return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
        }

    }

    private fun load(
        request: Request,
        target: Target,
        fetcher: Fetcher,
        cache: ImageCache.CachingStrategy,
        tag: String?
    ) {
        target.onProcessing()
        coroutineScope.launch {
            val cacheKey: String = tag ?: request.asString()

            val result: ImageResult<Bitmap> = imageCache[cacheKey]?.let {
                ImageResult.Success(it)
            } ?: run {
                fetcher.fetch(request)
            }
            submit(target, result, cache, cacheKey)
        }
    }

    @Synchronized
    fun abort() {
        coroutineScope.coroutineContext.cancelChildren()
    }

    fun clear() {
        imageCache.clear()
    }

    fun cache(): ImageCache {
        return imageCache
    }

    private suspend fun submit(
        target: Target,
        result: ImageResult<Bitmap>,
        cache: ImageCache.CachingStrategy,
        cacheKey: String
    ) {
        when (result) {
            is ImageResult.Success -> {
                imageCache.put(cacheKey, result.value, cache)
                withContext(Dispatchers.Main) {
                    target.onSuccess(result.value)
                }
            }
            is ImageResult.Error -> {
                withContext(Dispatchers.Main) {
                    target.onError()
                }
            }
        }
    }

}