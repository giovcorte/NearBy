package com.nearbyapp.nearby.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.PolylineOptions
import com.nearbyapp.nearby.R
import com.nearbyapp.nearby.components.ResponseWrapper
import com.nearbyapp.nearby.components.StandardAction
import com.nearbyapp.nearby.converters.PolylineParser
import com.nearbyapp.nearby.model.HomeCategory
import com.nearbyapp.nearby.model.ListWrapper
import com.nearbyapp.nearby.model.MapWrapper
import com.nearbyapp.nearby.model.detail.Detail
import com.nearbyapp.nearby.model.nearby.Photo
import com.nearbyapp.nearby.recycler.Identifiable
import com.nearbyapp.nearby.repository.DataSource
import org.json.JSONObject


abstract class AbstractDetailViewModel(application: Application): BaseViewModel(application) {

    val details: MutableLiveData<List<Identifiable>> = MutableLiveData()

    abstract fun loadDetails(id: String, dataSource: DataSource)

    fun getPolyline(response: ResponseWrapper<JSONObject?>): PolylineOptions? {
        return when(response) {
            is ResponseWrapper.Success -> {
                PolylineParser.parsePolylineOptions(response.value)
            }
            else -> null
        }
    }

    fun getImages(photos: List<Photo>?) : Identifiable? {
        photos?.also {
            return ListWrapper(it.toMutableList(), true)
        }
        return null
    }

    fun getReviews(detail: Detail): Identifiable? {
        detail.reviews?.let {
            return ListWrapper(it.mapIndexed { i, r ->
                r.page = (i + 1).toString()
                r
            }.toMutableList(), true)
        }
        return null
    }

    fun getMap(detail: Detail, userLat: Double, userLng: Double, polylineOptions: PolylineOptions?): Identifiable? {
        detail.geometry?.location?.let {
            return MapWrapper(it.lat, it.lng, userLat, userLng, polylineOptions, preferencesManager.getTravelModeString())
        }
        return null
    }

    fun getPhone(detail: Detail) : Identifiable {
        return HomeCategory(category = "Chiama", image = R.drawable.ic_call_36dp.toString(), standardAction = StandardAction.CALL_PHONE, data = detail.formatted_phone_number)
    }

    fun getWebSite(detail: Detail) : Identifiable {
        return HomeCategory(category = "Apri il sito",
                image = R.drawable.ic_website_36dp.toString(), standardAction = StandardAction.OPEN_BROWSER, data = detail.website)
    }

}