package com.nearbyapp.nearby.model.nearby

import com.google.gson.annotations.SerializedName

data class Southwest (

	@SerializedName("lat") val lat : Double,
	@SerializedName("lng") val lng : Double
)