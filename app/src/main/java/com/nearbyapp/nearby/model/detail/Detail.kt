package com.nearbyapp.nearby.model.detail

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.databinding.annotations.BindableObject
import com.google.gson.annotations.SerializedName
import com.nearbyapp.nearby.model.nearby.Geometry
import com.nearbyapp.nearby.model.nearby.Photo
import com.nearbyapp.nearby.recycler.Identifiable
import com.nearbyapp.nearby.widget.ItemDetail

@Entity
@BindableObject(view = ItemDetail::class)
data class Detail (
    @SerializedName("formatted_address") @ColumnInfo(name = "address") var formatted_address : String,
    @SerializedName("formatted_phone_number") @ColumnInfo var formatted_phone_number : String = "",
    @SerializedName("geometry") @ColumnInfo var geometry : Geometry? = null,
    @SerializedName("name") @ColumnInfo var place_name : String,
    @SerializedName("opening_hours") @Embedded var opening_hours : OpeningHours?,
    @SerializedName("photos") @ColumnInfo var photos : List<Photo>?,
    @SerializedName("place_id") @PrimaryKey var place_id : String,
    @SerializedName("price_level") @ColumnInfo(name = "price") var price_level : Int,
    @SerializedName("rating") @ColumnInfo var rating : Double?,
    @SerializedName("reviews") @ColumnInfo var reviews : List<Review>?,
    @SerializedName("website") @ColumnInfo var website : String = ""
) : Identifiable {

    override fun name(): String {
        return "Detail"
    }

    val storedThumbnail: String?
        get() {
            return photos?.first()?.id
        }

    val thumbnail: String?
        get() {
            photos?.let {
                return it.first().link
            }
            return null
        }

    val ratingText: String
        get() {
            rating?.also {
                return "Rating $rating /5"
            }
            return "Rating ?"
        }

    val priceText: String
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

    override fun id(): String {
        return place_id
    }

}