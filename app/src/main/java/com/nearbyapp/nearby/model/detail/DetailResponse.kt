package com.nearbyapp.nearby.model.detail

import com.google.gson.annotations.SerializedName

data class DetailResponse (

	@SerializedName("html_attributions") val html_attributions : List<String>,
	@SerializedName("result") val detail : Detail,
	@SerializedName("status") val status : String
)