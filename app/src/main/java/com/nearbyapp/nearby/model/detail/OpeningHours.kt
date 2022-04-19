package com.nearbyapp.nearby.model.detail

import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.google.gson.annotations.SerializedName
import com.nearbyapp.nearby.widget.ItemCalendar


@BindableObject(view = ItemCalendar::class)
data class OpeningHours (

    @SerializedName("open_now") @ColumnInfo var open_now : Boolean,
    @SerializedName("weekday_text")  @ColumnInfo var weekday_text : List<String>?
) : IData {
    override fun name(): String {
        return "OpeningHours"
    }

    val calendar: String
        get() {
            weekday_text?.let {
                return (it[0] + System.getProperty("line.separator")
                        + it[1] + System.getProperty("line.separator")
                        + it[2] + System.getProperty("line.separator")
                        + it[3] + System.getProperty("line.separator")
                        + it[4] + System.getProperty("line.separator")
                        + it[5] + System.getProperty("line.separator")
                        + it[6] + System.getProperty("line.separator"))
            }
            return "Nessuna informazione sugli orari"
        }

}