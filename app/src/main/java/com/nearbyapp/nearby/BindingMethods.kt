package com.nearbyapp.nearby

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.databinding.annotations.BindingMethod
import com.databinding.annotations.Data
import com.databinding.annotations.Inject
import com.databinding.annotations.View
import com.databinding.databinding.AdapterDataBinding
import com.databinding.databinding.DataBinding
import com.databinding.databinding.adapter.GenericRecyclerViewAdapter
import com.databinding.databinding.factory.ViewFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.nearbyapp.nearby.components.Clipboard
import com.nearbyapp.nearby.components.ImageStorageHelper
import com.nearbyapp.nearby.components.PreferencesManager
import com.nearbyapp.nearby.components.Status
import com.nearbyapp.nearby.loader.ImageLoader
import com.nearbyapp.nearby.model.*
import com.nearbyapp.nearby.model.detail.Detail
import com.nearbyapp.nearby.model.detail.OpeningHours
import com.nearbyapp.nearby.model.detail.Review
import com.nearbyapp.nearby.model.nearby.NearbyPlace
import com.nearbyapp.nearby.model.nearby.Photo
import com.nearbyapp.nearby.navigation.NavigationManager
import com.nearbyapp.nearby.widget.*

@SuppressLint("SetTextI18n")
object BindingMethods {

    @JvmStatic
    @BindingMethod
    fun bindNearbyPlaceWrapper(
        @View view: ItemNearbyPlace?,
        @Data data: NearbyPlaceWrapper?,
        @Inject imageStorage: ImageStorageHelper,
        @Inject navigation: NavigationManager,
        @Inject clipboard: Clipboard
    ) {
        safeLet(view, data?.detail) {v, detail ->
            imageStorage.getImageFile(detail.storedThumbnail)?.let {
                ImageLoader.get().load(it).into(v.image).run()
            }
            v.setOnClickListener {
                clipboard.putData("id", detail.place_id)
                clipboard.putData("name", detail.place_name)
                navigation.navigateTo("savedDetail")
            }
        }
    }

    @JvmStatic
    @BindingMethod
    fun bindItemCheckBoxes(@View view: ItemTextCheckbox?, @Data data: TravelModePreference?, @Inject preferencesManager: PreferencesManager?) {
        safeLet(view, data) { checkboxes, pref ->
            checkboxes.checkBox.isChecked = pref.first.selected
            checkboxes.checkBox1.isChecked = pref.second.selected
            var modeSelected = if (pref.first.selected) pref.first.name else pref.second.name
            checkboxes.checkBox.setOnClickListener {
                if (modeSelected == pref.first.name) {
                    checkboxes.checkBox.isChecked = true
                }
                modeSelected = pref.first.name
                checkboxes.checkBox1.isChecked = false
                preferencesManager?.putTravelMode(pref.first.name)
            }
            checkboxes.checkBox1.setOnClickListener {
                if (modeSelected == pref.second.name) {
                    checkboxes.checkBox1.isChecked = true
                }
                modeSelected = pref.second.name
                checkboxes.checkBox.isChecked = false
                preferencesManager?.putTravelMode(pref.second.name)
            }
        }
    }

    @JvmStatic
    @BindingMethod
    fun bindItemSeekBar(
        @View view: ItemTextSeekBar?,
        @Data data: RadiusPreference?,
        @Inject preferencesManager: PreferencesManager?
    ) {
        safeLet(view, data) { seekView, seekData ->
            seekView.text.text = seekView.context.getString(R.string.radius_preference_desc)

            val max = seekData.max
            val min = seekData.min
            val current = seekData.current
            seekView.seekBar.max = max
            seekView.seekBar.min = min

            if (current in min..max) {
                seekView.seekBar.progress = current
            }

            seekView.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    seekView.current.text = "$p1 km"
                    preferencesManager?.putRadius(p1)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }
            })
        }
    }

    @JvmStatic
    @BindingMethod
    fun bindItemCalendar(@View view: ItemCalendar?, @Data data: OpeningHours?) {
        view?.title?.text = view?.context?.getString(R.string.opening_hours_title)
    }

    @JvmStatic
    @BindingMethod
    fun bindItemImage(
        @View view: ItemPhoto?,
        @Data data: Photo?,
        @Inject imageStorage: ImageStorageHelper
    ) {
        safeLet(view?.image, data) { imageView, photo ->
            if (imageStorage.hasImageFile(photo.id)) {
                val file = imageStorage.getImageFile(photo.id)
                ImageLoader.get().load(file!!).into(imageView).run()
            } else {
                val drawable = CircularProgressDrawable(imageView.context)
                drawable.setColorSchemeColors(
                    color(imageView.context, R.color.colorPrimary),
                    color(imageView.context, R.color.colorPrimaryDark),
                    color(imageView.context, R.color.colorAccent))
                drawable.centerRadius = 30f
                drawable.strokeWidth = 5f
                ImageLoader.get().load(photo.link).into(imageView, drawable).run()
            }
        }
    }

    @JvmStatic
    @BindingMethod
    fun bindItemReview(@View view: ItemReview?, @Data data: Review?) {

    }

    @JvmStatic
    @BindingMethod
    fun bindItemDetail(@View view: ItemDetail?, @Data data: Detail?) {

    }

    @JvmStatic
    @BindingMethod
    fun bindItemMap(@View view: ItemMap?, @Data data: MapWrapper?) {
        safeLet(view?.mapView, data) { mapView, mapData ->
            mapView.onCreate(null)
            mapView.onResume()
            mapView.getMapAsync { googleMap ->
                googleMap.setOnMapLoadedCallback {
                    val latLngBounds = LatLngBounds.Builder()
                        .include(LatLng(mapData.lat, mapData.lng))
                        .include(LatLng(mapData.userLat, mapData.userLng))
                        .build()
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100))
                    googleMap.addMarker(MarkerOptions().position(LatLng(mapData.lat, mapData.lng)))
                    googleMap.addMarker(MarkerOptions().position(LatLng(mapData.userLat, mapData.userLng)))
                    mapData.polylineOptions?.let {
                        if (mapData.travelMode == AppConstants.WALKING) {
                            it.color(ContextCompat.getColor(mapView.context, R.color.green))
                        } else {
                            it.color(ContextCompat.getColor(mapView.context, R.color.orange))
                        }
                        googleMap.addPolyline(it)
                    }
                }
            }
        }
    }

    @JvmStatic
    @BindingMethod
    fun bindItemList(@View view: ItemCardList?, @Data data: ListWrapper?) {
        safeLet(view?.list, data) { recyclerView, listData ->
            val linearLayoutManager: LinearLayoutManager
            if (listData.horizontal) {
                linearLayoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.HORIZONTAL, false)
                if (recyclerView.onFlingListener == null) {
                    val snapHelper = PagerSnapHelper()
                    snapHelper.attachToRecyclerView(recyclerView)
                }
            } else {
                linearLayoutManager = LinearLayoutManager(recyclerView.context)
            }
            val adapterDataBinding = AdapterDataBinding(DataBinding.instance)
            val viewFactory = ViewFactory(recyclerView.context)
            recyclerView.layoutManager = linearLayoutManager
            recyclerView.adapter = GenericRecyclerViewAdapter(adapterDataBinding, viewFactory, listData.list)
        }
    }

    @JvmStatic
    @BindingMethod
    fun bindText(@View view: TextView?, @Data data: String?) {
        view?.text = data
    }

    @JvmStatic
    @BindingMethod
    fun bindImage(@View view: ImageView?, @Data data: String?) {
        safeLet(view, data) { v, s ->
            if (Utils.isNumber(s)) {
                ImageLoader.get().load(s.toInt()).into(v).run()
            } else {
                val drawable = CircularProgressDrawable(v.context)
                drawable.setColorSchemeColors(
                    color(v.context, R.color.colorPrimary),
                    color(v.context, R.color.colorPrimaryDark),
                    color(v.context, R.color.colorAccent))
                drawable.centerRadius = 30f
                drawable.strokeWidth = 5f
                ImageLoader.get().load(s).into(v, drawable).run()
            }
        }
    }

    @JvmStatic
    @BindingMethod
    fun bindErrorView(@View view: ErrorView?, @Data data: Status?) {
        view?.image?.setImageResource(data!!.image)
        view?.text?.text = data!!.text
    }

    @JvmStatic
    @BindingMethod
    fun bindItemHome(@View view: ItemHome?, @Data data: HomeCategory?) {
        view?.text?.typeface = Typeface.DEFAULT_BOLD
    }

    @JvmStatic
    @BindingMethod
    fun bindNearbyPlace(@View view: ItemNearbyPlace?, @Data data: NearbyPlace?, @Inject navigation: NavigationManager, @Inject clipboard: Clipboard) {
        view?.image?.visibility = if (data?.thumbnail != null) VISIBLE else GONE
        safeLet(view, data?.thumbnail) { v, thumbnail ->
            val drawable = CircularProgressDrawable(v.context)
            drawable.setColorSchemeColors(
                color(v.context, R.color.colorPrimary),
                color(v.context, R.color.colorPrimaryDark),
                color(v.context, R.color.colorAccent))
            drawable.centerRadius = 30f
            drawable.strokeWidth = 5f
            ImageLoader.get().load(thumbnail).into(v.image, drawable).tag(data!!.place_id).run()
        }
        view?.setOnClickListener {
            clipboard.putData("name", data!!.name)
            clipboard.putData("id", data.place_id)
            clipboard.putData("lat", data.userLat)
            clipboard.putData("lng", data.userLng)
            navigation.navigateTo("detail")
        }
    }

    @JvmStatic
    fun color(context: Context, id: Int) : Int {
        return ContextCompat.getColor(context, id)
    }

    @JvmStatic
    @BindingMethod
    fun bindOpeningHours(@View view: TextView?, @Data data: OpeningHours?) {
        data?.open_now?.let {
            if (it) {
                view?.text = view?.context?.getString(R.string.open)
            } else {
                view?.text = view?.context?.getString(R.string.closed)
            }
        }
    }

    @JvmStatic
    @BindingMethod
    fun bindItemProgress(@View view: ItemSpinner?, @Data data: ProgressWrapper?) {

    }

    @JvmStatic
    @BindingMethod
    fun bindItemText(@View view: ItemText?, @Data data: TextWrapper?) {

    }

    private inline fun <T1: Any, T2: Any, R: Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2)->R?): R? {
        return if (p1 != null && p2 != null) block(p1, p2) else null
    }

}