package com.nearbyapp.nearby.model.detail

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.google.gson.annotations.SerializedName
import com.nearbyapp.nearby.widget.ItemCalendar


@BindableObject(view = ItemCalendar::class)
data class OpeningHours (

    @SerializedName("open_now") val open_now : Boolean,
    @SerializedName("periods") val periods : List<Periods>?,
    @SerializedName("weekday_text") val weekday_text : List<String>?
) : IData {
    override fun name(): String {
        return "OpeningHours"
    }

    val calendar: String
        get() {
            weekday_text?.let {
                return (weekday_text[0] + System.getProperty("line.separator")
                        + weekday_text[1] + System.getProperty("line.separator")
                        + weekday_text[2] + System.getProperty("line.separator")
                        + weekday_text[3] + System.getProperty("line.separator")
                        + weekday_text[4] + System.getProperty("line.separator")
                        + weekday_text[5] + System.getProperty("line.separator")
                        + weekday_text[6] + System.getProperty("line.separator"))
            }
            return "No opining hours information provided for this place"
        }

}