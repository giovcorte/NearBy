package com.nearbyapp.nearby.model.detail

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.google.gson.annotations.SerializedName
import com.nearbyapp.nearby.model.ListWrapper
import com.nearbyapp.nearby.model.nearby.Geometry
import com.nearbyapp.nearby.model.nearby.Photo
import com.nearbyapp.nearby.widget.ItemDetail

@BindableObject(view = ItemDetail::class)
data class Detail (

    @SerializedName("address_components") val address_components : List<AddressComponents>,
    @SerializedName("formatted_address") val formatted_address : String,
    @SerializedName("formatted_phone_number") val formatted_phone_number : String,
    @SerializedName("geometry") val geometry : Geometry?,
    @SerializedName("name") val name : String,
    @SerializedName("opening_hours") val opening_hours : OpeningHours?,
    @SerializedName("photos") val photos : List<Photo>?,
    @SerializedName("place_id") val place_id : String,
    @SerializedName("price_level") val price_level : Int,
    @SerializedName("rating") val rating : Double?,
    @SerializedName("reviews") val reviews : List<Review>?
) : IData {
    override fun name(): String {
        return "Detail"
    }

    val rating_text: String
        get() {
            rating?.also {
                return "Rating $rating /5"
            }
            return "Rating ?"
        }

    val price_text: String
        get() {
            return if (price_level > 0) {
                var cash = ""
                for (i in 1..price_level) {
                    cash += cash + "€"
                }
                cash
            } else {
                "No price"
            }
        }

}