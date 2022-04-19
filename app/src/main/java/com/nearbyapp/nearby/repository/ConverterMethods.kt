package com.nearbyapp.nearby.repository

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.nearbyapp.nearby.model.detail.Review
import com.nearbyapp.nearby.model.nearby.Geometry
import com.nearbyapp.nearby.model.nearby.Photo

object ConverterMethods {
    @TypeConverter
    fun listToJsonString(value: List<String>?): String = Gson().toJson(value)

    @TypeConverter
    fun jsonStringToList(value: String) = Gson().fromJson(value, Array<String>::class.java).toList()

    @TypeConverter
    fun listToJsonReview(value: List<Review>?): String = Gson().toJson(value)

    @TypeConverter
    fun jsonReviewToList(value: String) = Gson().fromJson(value, Array<Review>::class.java).toList()

    @TypeConverter
    fun listToJsonPhoto(value: List<Photo>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonPhotoToList(value: String) = Gson().fromJson(value, Array<Photo>::class.java).toList()

    @TypeConverter
    fun geometryToJson(value: Geometry?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToGeometry(value: String) = Gson().fromJson(value, Geometry::class.java)
}