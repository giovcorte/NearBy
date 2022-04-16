package com.nearbyapp.nearby.model.detail

import com.google.gson.annotations.SerializedName

data class Periods (

    @SerializedName("close") val close : Close,
    @SerializedName("open") val open : Open
)