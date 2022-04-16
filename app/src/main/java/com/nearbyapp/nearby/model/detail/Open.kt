package com.nearbyapp.nearby.model.detail

import com.google.gson.annotations.SerializedName

data class Open (

	@SerializedName("day") val day : Int,
	@SerializedName("time") val time : Int
)