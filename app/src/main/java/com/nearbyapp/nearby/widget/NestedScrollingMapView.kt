package com.nearbyapp.nearby.widget

import android.content.Context
import android.util.AttributeSet
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import android.view.MotionEvent

class NestedScrollingMapView : MapView {
    private var pointers = 0
    private var twoFingerScroll = true
    private var map: GoogleMap? = null

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs!!, defStyle
    )

    fun setTwoFingerScroll(twoFingerScroll: Boolean) {
        this.twoFingerScroll = twoFingerScroll
    }

    override fun getMapAsync(callback: OnMapReadyCallback) {
        super.getMapAsync { googleMap: GoogleMap? ->
            map = googleMap
            callback.onMapReady(googleMap!!)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (twoFingerScroll && map != null) {
            when (ev.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> pointers++
                MotionEvent.ACTION_POINTER_UP -> if (pointers > 0) {
                    pointers--
                }
                MotionEvent.ACTION_UP -> pointers = 0
                MotionEvent.ACTION_DOWN -> pointers = 1
            }
            if (pointers > 1) {
                disableParentScrolling()
            } else {
                enableParentScrolling()
            }
        } else {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun enableParentScrolling() {
        map!!.uiSettings.setAllGesturesEnabled(false)
        parent.requestDisallowInterceptTouchEvent(false)
    }

    private fun disableParentScrolling() {
        map!!.uiSettings.setAllGesturesEnabled(true)
        parent.requestDisallowInterceptTouchEvent(true)
    }

}