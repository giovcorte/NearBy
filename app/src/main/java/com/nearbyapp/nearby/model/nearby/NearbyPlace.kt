package com.nearbyapp.nearby.model.nearby

import com.databinding.annotations.BindableObject
import com.databinding.databinding.IData
import com.google.gson.annotations.SerializedName
import com.nearbyapp.nearby.recycler.Identifiable
import com.nearbyapp.nearby.model.detail.OpeningHours
import com.nearbyapp.nearby.widget.ItemNearbyPlace

@BindableObject(view = ItemNearbyPlace::class)
data class NearbyPlace (

    @SerializedName("business_status") val business_status : String,
    @SerializedName("geometry") val geometry : Geometry,
    @SerializedName("icon") val icon : String,
    @SerializedName("icon_background_color") val icon_background_color : String,
    @SerializedName("icon_mask_base_uri") val icon_mask_base_uri : String,
    @SerializedName("name") val name : String,
    @SerializedName("opening_hours") val opening_hours : OpeningHours?,
    @SerializedName("photos") val photos : List<Photo>?,
    @SerializedName("place_id") val place_id : String,
    @SerializedName("plus_code") val plus_code : PlusCode,
    @SerializedName("price_level") val price_level : Int,
    @SerializedName("rating") val rating : Double,
    @SerializedName("reference") val reference : String,
    @SerializedName("scope") val scope : String,
    @SerializedName("types") val types : List<String>,
    @SerializedName("user_ratings_total") val user_ratings_total : Int,
    @SerializedName("vicinity") val vicinity : String
): IData, Identifiable {
    override fun name(): String {
        return "NearbyPlace"
    }

    val thumbnail: String?
        get() {
            return if (!photos.isNullOrEmpty()) {
                photos[0].link
            } else {
                null
            }
        }

    val price: String
        get() {
            return if (price_level > 0) {
                var cash = ""
                for (i in 1..price_level) {
                    cash += cash + "€"
                }
                cash
            } else {
                ""
            }
        }

    var userLat: Double = 0.0
    var userLng: Double = 0.0

    override fun id(): String {
        return place_id
    }
}