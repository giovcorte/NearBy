package com.nearbyapp.nearby.model.detail

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.google.gson.annotations.SerializedName
import com.nearbyapp.nearby.widget.ItemReview

@BindableObject(view = ItemReview::class)
data class Review (

	@SerializedName("author_name") val author_name : String,
	@SerializedName("author_url") val author_url : String,
	@SerializedName("language") val language : String,
	@SerializedName("profile_photo_url") val profile_photo_url : String,
	@SerializedName("rating") val rating : Int?,
	@SerializedName("relative_time_description") val relative_time_description : String,
	@SerializedName("text") val text : String?,
	@SerializedName("time") val time : Int
) : IData {

	var page: String? = null

	val rating_text: String
		get() {
			rating?.also {
				return "Rating $rating /5"
			}
			return "Rating -"
		}

	override fun name(): String {
		return "Review"
	}

}