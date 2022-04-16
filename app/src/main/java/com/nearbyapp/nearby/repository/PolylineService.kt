package com.nearbyapp.nearby.repository

import com.nearbyapp.nearby.converters.JSONConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface PolylineService {

    @GET("api/directions/json")
    suspend fun getPolyline(
        @Query("origin") origin: String?,
        @Query("destination") destination: String?,
        @Query("mode") mode: String?,
        @Query("key") key: String?
    ): JSONObject?

    companion object {
        private var retrofitService: PolylineService? = null
        fun getInstance() : PolylineService {

            val client = OkHttpClient.Builder()
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            client.addInterceptor(loggingInterceptor)

            if (retrofitService == null) {
                val retrofit = Retrofit.Builder()
                    .client(client.build())
                    .baseUrl("https://maps.googleapis.com/maps/")
                    .addConverterFactory(JSONConverterFactory.create())
                    .build()
                retrofitService = retrofit.create(PolylineService::class.java)
            }
            return retrofitService!!
        }

    }
}