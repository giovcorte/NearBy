package com.nearbyapp.nearby.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nearbyapp.nearby.model.detail.Detail

@Dao
interface PlacesDAO {

    @Query("SELECT * FROM detail")
    suspend fun getAllPlaces(): List<Detail>

    @Query("SELECT * FROM detail WHERE place_id == :id")
    suspend fun getPlace(id: String): Detail?

    @Query("SELECT EXISTS (SELECT 1 FROM detail WHERE place_id = :id)")
    suspend fun existPlace(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: Detail)

    @Query("DELETE FROM detail WHERE place_id = :id")
    suspend fun deleteDetail(id: String)

    @Query("DELETE FROM detail")
    suspend fun deleteAll()

}