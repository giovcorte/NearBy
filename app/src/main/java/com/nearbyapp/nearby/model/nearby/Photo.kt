package com.nearbyapp.nearby.model.nearby

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.google.gson.annotations.SerializedName
import com.nearbyapp.nearby.widget.ItemPhoto

@BindableObject(ItemPhoto::class)
data class Photo (
	@SerializedName("height") val height : Int,
	@SerializedName("html_attributions") val html_attributions : List<String>,
	@SerializedName("photo_reference") val photo_reference : String,
	@SerializedName("width") val width : Int
): IData {
	val link: String
		get() {
			return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=$width&photoreference=$photo_reference&key=AIzaSyA6H30gsKS5UGyR30_CxE1TygjPup6wyOM"
		}

	override fun name(): String {
		return "Photo"
	}
}