package com.nearbyapp.nearby.model.nearby

import com.google.gson.annotations.SerializedName

data class NearbySearchResponse (

	@SerializedName("html_attributions") val html_attributions : List<String>,
	@SerializedName("next_page_token") val next_page_token : String?,
	@SerializedName("results") val results : MutableList<NearbyPlace>,
	@SerializedName("status") val status : String
)