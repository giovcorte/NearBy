package com.nearbyapp.nearby.repository

import com.diskcache.diskcache.Utils
import com.google.gson.Gson
import com.nearbyapp.nearby.BuildConfig
import com.nearbyapp.nearby.components.ResponseWrapper
import com.nearbyapp.nearby.components.Status
import com.nearbyapp.nearby.loader.cache.diskcache.DiskCache
import com.nearbyapp.nearby.model.detail.Detail
import com.nearbyapp.nearby.model.detail.DetailResponse
import com.nearbyapp.nearby.model.nearby.NearbySearchResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.nio.charset.StandardCharsets
import kotlin.reflect.KClass

class RepositoryImpl(
    private val placesService: PlacesService,
    private val polylineService: PolylineService,
    private val placesDAO: PlacesDAO,
    private val diskCache: DiskCache
) : Repository {

    companion object {
        const val NO_CACHE = "no-cache"
    }

    val gson = Gson()
    var serviceErrorCallBack: Repository.ServiceCallBack? = null

    suspend fun getNearbyPlaces(
        lat: Double,
        lng: Double,
        radius: Int,
        query: String,
        dataSource: DataSource
    ): ResponseWrapper<NearbySearchResponse?> {
        return when (dataSource) {
            DataSource.CACHE -> {
                getResponseFromCache(query, NearbySearchResponse::class)
            }
            else -> {
                safeApiCall(query, Dispatchers.IO) {
                    placesService.getPlaces(
                        "$lat,$lng",
                        radius.toString(),
                        query,
                        BuildConfig.GOOGLE_API_KEY
                    )
                }
            }
        }
    }

    suspend fun getMorePlaces(token: String, source: DataSource): ResponseWrapper<NearbySearchResponse?> {
        val cached = getResponseFromCache(token, NearbySearchResponse::class)
        if (cached.value != null) {
            return cached
        }
        return when (source) {
            DataSource.CACHE -> {getResponseFromCache(token, NearbySearchResponse::class)}
            else -> {
                safeApiCall(token, Dispatchers.IO) {
                    placesService.getMorePlaces(token, BuildConfig.GOOGLE_API_KEY)
                }
            }
        }
    }

    suspend fun getPlaceDetail(id: String, dataSource: DataSource): ResponseWrapper<DetailResponse?> {
        return when {
            dataSource == DataSource.CACHE -> {
                getResponseFromCache(id, DetailResponse::class)
            }
            existPlace(id) || dataSource == DataSource.DATABASE -> {
                ResponseWrapper.Success(wrapDatabaseStoredPlace(placesDAO.getPlace(id)!!))
            }
            else -> {
                safeApiCall(id, Dispatchers.IO) {
                    placesService.getPlace(id, BuildConfig.GOOGLE_API_KEY)
                }
            }
        }
    }

    private fun <T: Any> getResponseFromCache(key: String, clazz: KClass<T>) : ResponseWrapper.Success<T?> {
        return diskCache.get(format(key))?.let { snapshot ->
            val cachedResponse = snapshot.file().inputStream().use {
                it.readBytes().toString(Charsets.UTF_8)
            }
            snapshot.close()
            val response = gson.fromJson(cachedResponse, clazz.java)
            ResponseWrapper.Success(response).apply { fromCache = true }
        } ?: run {
            ResponseWrapper.Success(null)
        }
    }

    private fun <T> putResponseToCache(key: String, response: ResponseWrapper.Success<T>) {
        when {
            key != NO_CACHE -> {
                val responseString = gson.toJson(response.value).toString()
                diskCache.edit(format(key))?.let { editor ->
                    editor.file().bufferedWriter(StandardCharsets.UTF_8).use { writer ->
                        writer.write(responseString)
                    }
                    editor.commit()
                }
            }
        }
    }

    suspend fun getPolyline(lat: Double, lng: Double, lat1: Double, lng1: Double): ResponseWrapper<JSONObject?> {
        return safeApiCall(NO_CACHE, Dispatchers.IO) {
            polylineService.getPolyline("$lat,$lng", "$lat1,$lng1", "walking", BuildConfig.GOOGLE_API_KEY)
        }
    }

    suspend fun existPlace(id: String): Boolean {
        return placesDAO.existPlace(id)
    }

    suspend fun getStoredPlaces(): List<Detail> {
        return placesDAO.getAllPlaces()
    }

    suspend fun savePlaceDetail(detail: Detail) {
        placesDAO.insertDetail(detail)
    }

    suspend fun deletePlaceDetail(id: String) {
        placesDAO.deleteDetail(id)
    }

    private fun wrapDatabaseStoredPlace(detail: Detail): DetailResponse {
        return DetailResponse(emptyList(), detail, "OK")
    }

    private suspend fun <T> safeApiCall(cacheKey: String, dispatcher: CoroutineDispatcher, apiCall: suspend () -> T): ResponseWrapper<T> {
        return withContext(dispatcher) {
            try {
                val response = ResponseWrapper.Success(apiCall.invoke())
                putResponseToCache(cacheKey, response)
                response
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> {
                        serviceErrorCallBack?.onError()
                        ResponseWrapper.Error(Status.INTERNET)
                    }
                    is HttpException -> {
                        serviceErrorCallBack?.onError()
                        ResponseWrapper.Error(Status.SERVICE)
                    }
                    else -> {
                        serviceErrorCallBack?.onError()
                        ResponseWrapper.Error(Status.GENERIC)
                    }
                }
            }
        }
    }

    private fun format(s: String) : String {
        return Utils.formatKey(s.lowercase())
    }

    fun registerServiceCallback(callBack: Repository.ServiceCallBack) {
        this.serviceErrorCallBack = callBack
    }

}