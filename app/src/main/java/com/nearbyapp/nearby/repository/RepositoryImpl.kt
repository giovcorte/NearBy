package com.nearbyapp.nearby.repository

import com.nearbyapp.nearby.BuildConfig
import com.nearbyapp.nearby.components.ResponseWrapper
import com.nearbyapp.nearby.components.Status
import com.nearbyapp.nearby.model.detail.Detail
import com.nearbyapp.nearby.model.detail.DetailResponse
import com.nearbyapp.nearby.model.nearby.NearbySearchResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class RepositoryImpl(
    private val placesService: PlacesService,
    private val polylineService: PolylineService,
    private val placesDAO: PlacesDAO
) : Repository {

    var serviceErrorCallBack: Repository.ServiceCallBack? = null

    suspend fun getNearbyPlaces(lat: Double, lng: Double, radius: Int, query: String): ResponseWrapper<NearbySearchResponse?> {
        return safeApiCall(Dispatchers.IO) {
            placesService.getPlaces("$lat,$lng", radius.toString(), query, BuildConfig.GOOGLE_API_KEY)
        }
    }

    suspend fun getMorePlaces(token: String): ResponseWrapper<NearbySearchResponse?> {
        return safeApiCall(Dispatchers.IO) {
            placesService.getMorePlaces(token, BuildConfig.GOOGLE_API_KEY)
        }
    }

    suspend fun getPlaceDetail(id: String): ResponseWrapper<DetailResponse?> {
        if (existPlace(id)) {
            return ResponseWrapper.Success(wrapDatabaseStoredPlace(getStoredPlace(id)!!))
        }
        return safeApiCall(Dispatchers.IO) { placesService.getPlace(id, BuildConfig.GOOGLE_API_KEY) }
    }

    suspend fun getPolyline(lat: Double, lng: Double, lat1: Double, lng1: Double): ResponseWrapper<JSONObject?> {
        return safeApiCall(Dispatchers.IO) {
            polylineService.getPolyline("$lat,$lng", "$lat1,$lng1", "walking", BuildConfig.GOOGLE_API_KEY)
        }
    }

    suspend fun existPlace(id: String): Boolean {
        return placesDAO.existPlace(id)
    }

    suspend fun getStoredPlaces(): List<Detail> {
        return placesDAO.getAllPlaces()
    }

    suspend fun getStoredPlace(id: String): Detail? {
        return placesDAO.getPlace(id)
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

    private suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, apiCall: suspend () -> T): ResponseWrapper<T> {
        return withContext(dispatcher) {
            try {
                ResponseWrapper.Success(apiCall.invoke())
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

    fun registerServiceCallback(callBack: Repository.ServiceCallBack) {
        this.serviceErrorCallBack = callBack
    }

}