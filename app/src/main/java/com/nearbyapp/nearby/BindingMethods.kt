package com.nearbyapp.nearby

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
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
import com.databinding.databinding.DataBindingHelper
import com.databinding.databinding.IViewAction
import com.databinding.databinding.adapter.GenericRecyclerViewAdapter
import com.databinding.databinding.factory.ViewFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.nearbyapp.nearby.components.*
import com.nearbyapp.nearby.loader.ImageLoader
import com.nearbyapp.nearby.model.*
import com.nearbyapp.nearby.model.detail.Detail
import com.nearbyapp.nearby.model.detail.OpeningHours
import com.nearbyapp.nearby.model.detail.Review
import com.nearbyapp.nearby.model.nearby.NearbyPlace
import com.nearbyapp.nearby.model.nearby.Photo
import com.nearbyapp.nearby.model.settings.CachePreference
import com.nearbyapp.nearby.model.settings.RadiusPreference
import com.nearbyapp.nearby.model.settings.TravelModePreference
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
                clipboard.wrapper("savedDetails").put("id", detail.place_id)
                clipboard.wrapper("savedDetails").put("name", detail.place_name)
                navigation.navigateTo("savedDetail")
            }
        }
    }

    @JvmStatic
    @BindingMethod
    fun bindItemSingleCheckbox(@View view: ItemSingleTextCheckbox?, @Data data: CachePreference?, @Inject preferencesManager: PreferencesManager?) {
        safeLet(view, data) { checkbox, pref ->
            checkbox.checkbox.isChecked = pref.selected
            checkbox.checkbox.setOnCheckedChangeListener { _, b ->
                preferencesManager?.putCacheEnabled(b)
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
                val drawable = getDefaultLoadingDrawable(imageView.context)
                ImageLoader.get().load(photo.link).into(imageView, drawable).run()
            }
        }
    }

    fun getDefaultLoadingDrawable(context: Context) : Drawable {
        val drawable = CircularProgressDrawable(context)
        drawable.setColorSchemeColors(
            color(context, R.color.colorPrimary),
            color(context, R.color.colorPrimaryDark),
            color(context, R.color.colorAccent))
        drawable.centerRadius = 30f
        drawable.strokeWidth = 5f
        return drawable
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
                val drawable = getDefaultLoadingDrawable(v.context)
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
    fun bindItemHome(@View view: ItemHome?, @Data data: HomeCategory?, @Inject navigation: NavigationManager) {
        view?.text?.typeface = Typeface.DEFAULT_BOLD
        DataBindingHelper.bindAction(view, object : IViewAction {
            override fun onClick() {
                handleStandardAction(data?.standardAction, data?.data, navigation.getActivityContext(), data?.action)
            }
        })
    }

    fun handleStandardAction(standardAction: StandardAction?, data: String?, context: Activity, default: IViewAction? = null) {
        when (standardAction) {
            StandardAction.CALL_PHONE -> {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.CALL_PHONE), 0)
                    return
                }
                if (data != null && data != "") {
                    val callIntent = Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:${data}"))
                    context.startActivity(callIntent)
                } else {
                    Toast.makeText(context, "Nessun numero di telefono disponibile", Toast.LENGTH_SHORT).show()
                }
            }
            StandardAction.OPEN_BROWSER -> {
                if (data != null && data != "") {
                    val browser = Intent(Intent.ACTION_VIEW, Uri.parse(data))
                    context.startActivity(browser)
                } else {
                    Toast.makeText(context, "Questo luogo non ha un sito web disponibile", LENGTH_SHORT).show()
                }
            }
            else -> {
                default?.onClick()
            }
        }
    }

    @JvmStatic
    @BindingMethod
    fun bindNearbyPlace(@View view: ItemNearbyPlace?, @Data data: NearbyPlace?, @Inject navigation: NavigationManager, @Inject clipboard: Clipboard) {
        view?.image?.visibility = if (data?.thumbnail != null) VISIBLE else GONE
        safeLet(view, data?.thumbnail) { v, thumbnail ->
            val drawable = getDefaultLoadingDrawable(v.context)
            ImageLoader.get().load(thumbnail).into(v.image, drawable).tag(data!!.place_id).run()
        }
        view?.setOnClickListener {
            clipboard.wrapper("detail").put("name", data!!.name)
            clipboard.wrapper("detail").put("id", data.place_id)
            clipboard.wrapper("detail").put("lat", data.userLat)
            clipboard.wrapper("detail").put("lng", data.userLng)
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
            when {
                it -> {
                    view?.text = view?.context?.getString(R.string.open)
                }
                else -> {
                    view?.text = view?.context?.getString(R.string.closed)
                }
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
        safeLet(view, data) { v, d ->
            if (d.matchParent) {
                v.container.layoutParams.height = ConstraintLayout.LayoutParams.MATCH_PARENT
            }
        }
    }

    private inline fun <T1: Any, T2: Any, R: Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2)->R?): R? {
        return if (p1 != null && p2 != null) block(p1, p2) else null
    }

}