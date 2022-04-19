package com.nearbyapp.nearby.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nearbyapp.nearby.model.detail.Detail

@Database(entities = [Detail::class], version = 1)
@TypeConverters(ConverterMethods::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun placesDAO(): PlacesDAO

    companion object {
        private const val DATABASE_NAME = "places_db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .build()
        }

    }
}