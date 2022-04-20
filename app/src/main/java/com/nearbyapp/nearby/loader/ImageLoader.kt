package com.nearbyapp.nearby.loader

import android.app.Application
import android.graphics.Bitmap
import android.widget.ImageView
import com.nearbyapp.nearby.loader.cache.ImageCache
import com.nearbyapp.nearby.loader.fetcher.FileFetcher
import com.nearbyapp.nearby.loader.fetcher.ResourceFetcher
import com.nearbyapp.nearby.loader.fetcher.URLFetcher
import com.nearbyapp.nearby.model.nearby.Photo
import kotlinx.coroutines.*
import java.io.File

class ImageLoader(application: Application) {

    private val coroutineScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)

    private val imageCache: ImageCache = ImageCache(application)

    private val urlFetcher: URLFetcher = URLFetcher()
    private val fileFetcher: FileFetcher = FileFetcher()
    private val resourceFetcher: ResourceFetcher = ResourceFetcher()

    private val requestBuilder: RequestBuilder = RequestBuilder(application)
    private val targetBuilder: TargetBuilder = TargetBuilder()

    private var listener: Listener? = null

    interface Listener {
        fun onWritingStarted()
        fun onWritingCompleted()
        fun onWritingFailed()
    }

    fun load(url: String, view: ImageView?) {
        coroutineScope.launch {
            val target = targetBuilder.create(view)
            val request = requestBuilder.create(target, url)
            val result: ImageResult<Bitmap> = if (cached(request.source())) {
                ImageResult.Success(imageCache[request.source()]!!)
            } else {
                urlFetcher.fetch(request)
            }
            submit(request, result)
        }
    }

    fun load(file: File, view: ImageView?) {
        coroutineScope.launch {
            val target = targetBuilder.create(view)
            val request = requestBuilder.create(target, file)
            val result: ImageResult<Bitmap> = if (cached(request.source())) {
                ImageResult.Success(imageCache[request.source()]!!)
            } else {
                fileFetcher.fetch(request)
            }
            submit(request, result)
        }
    }

    fun load(res: Int, view: ImageView?) {
        coroutineScope.launch {
            val target = targetBuilder.create(view)
            val request = requestBuilder.create(target, res)
            val result: ImageResult<Bitmap> = if (cached(request.source())) {
                ImageResult.Success(imageCache[request.source()]!!)
            } else {
                resourceFetcher.fetch(request)
            }
            submit(request, result)
        }
    }

    fun dumps(baseName: String, images: List<Photo>) {
        listener?.onWritingStarted()
        CoroutineScope(Dispatchers.Main).launch {
            val success: Boolean = withContext(coroutineScope.coroutineContext) {
                imageCache.dumps(baseName, images.map { it.link })
            }
            if (success) {
                listener?.onWritingCompleted()
            } else {
                listener?.onWritingFailed()
            }
        }
    }

    fun clear() {
        imageCache.clear()
    }

    fun cache(): ImageCache {
        return imageCache
    }

    private fun cached(source: String): Boolean {
        return imageCache[source] != null
    }

    private suspend fun submit(request: Request, result: ImageResult<Bitmap>) {
        withContext(Dispatchers.Main) {
            when (result) {
                is ImageResult.Success -> {
                    imageCache.put(request.source(), result.value, request.caching())
                    request.target().onSuccess(result.value)
                }
                is ImageResult.Error -> {
                    request.target().onError()
                }
            }
        }
    }

    fun registerListener(listener: Listener) {
        this.listener = listener
    }

    fun unregisterListener() {
        this.listener = null
    }

    enum class Status {
        IDLE,
        WRITING,
        COMPLETED,
        ERROR
    }

}