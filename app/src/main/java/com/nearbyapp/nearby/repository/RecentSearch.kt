package com.nearbyapp.nearby.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class RecentSearch(
    @PrimaryKey val key: String,
    @ColumnInfo val timestamp: Long,
    @ColumnInfo val values: List<String>
) {
}