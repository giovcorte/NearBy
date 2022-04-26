package com.nearbyapp.nearby.repository

import com.nearbyapp.nearby.model.detail.DetailResponse
import com.nearbyapp.nearby.model.nearby.NearbySearchResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesService {

    @GET("api/place/nearbysearch/json")
    suspend fun getPlaces(
        @Query("location") location: String?,
        @Query("radius") radius: String?,
        @Query("query") keyword: String?,
        @Query("key") key: String?
    ): NearbySearchResponse?

    @GET("api/place/nearbysearch/json")
    suspend fun getMorePlaces(
        @Query("pagetoken") pagetoken: String?,
        @Query("key") key: String?
    ): NearbySearchResponse?

    @GET("api/place/nearbysearch/json")
    suspend fun getPlacesByType(
        @Query("location") location: String?,
        @Query("radius") radius: String?,
        @Query("type") type: String?,
        @Query("key") key: String?
    ): NearbySearchResponse?

    @GET("api/place/details/json?fields=address_component,name,rating,formatted_phone_number,photo,opening_hours,formatted_address,website,geometry,place_id,price_level,reviews")
    suspend fun getPlace(@Query("place_id") id: String?, @Query("key") key: String?): DetailResponse?

    companion object {
        private var retrofitService: PlacesService? = null
        fun getInstance() : PlacesService {

            val client = OkHttpClient.Builder()
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            client.addInterceptor(loggingInterceptor)

            if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .client(client.build())
                    .baseUrl("https://maps.googleapis.com/maps/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                retrofitService = retrofit.create(PlacesService::class.java)
            }
            return retrofitService!!
        }

    }

    /*

    @GET("api/distancematrix/json?units=metrics&language=it-IT")
    suspend fun getDistance(
        @Query("destinations") destination: String?,
        @Query("origins") origins: String?,
        @Query("mode") mode: String?,
        @Query("key") key: String?
    ): Single<JSONObject?>?
     */

}