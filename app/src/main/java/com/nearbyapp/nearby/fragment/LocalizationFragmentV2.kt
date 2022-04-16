package com.nearbyapp.nearby.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

abstract class LocalizationFragmentV2 : ListFragment() {

    private var requestingLocationUpdates = false
    private var locationRequest: LocationRequest = LocationRequest.create()
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var permissionListener: PermissionListener? = null

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            stopLocationUpdates()
            requestingLocationUpdates = false
            onLocationChanged(locationResult.lastLocation)
        }
    }

    interface PermissionListener {
        fun onPrivacyReady()
        fun onSensorReady()
        fun onPermissionRejected(missingPermission: MissingPermission?)
    }

    enum class MissingPermission {
        LOCALIZATION_PERMISSION, SENSOR_PERMISSION
    }

    private val sensorPermissionLauncher =
        registerForActivityResult(StartIntentSenderForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (permissionListener == null) {
                    return@registerForActivityResult
                }
                permissionListener?.onSensorReady()
            } else {
                permissionListener?.onPermissionRejected(MissingPermission.SENSOR_PERMISSION)
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (permissionListener == null) {
                return@registerForActivityResult
            }
            if (isGranted) {
                permissionListener?.onPrivacyReady()
            } else {
                permissionListener?.onPermissionRejected(MissingPermission.LOCALIZATION_PERMISSION)
            }
        }

    protected abstract fun onLocationChanged(lastLocation: Location?)
    protected abstract fun onPermissionsReady()
    protected abstract fun onPermissionError(missingPermission: MissingPermission?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeLocationService()
        if (savedInstanceState != null && savedInstanceState.containsKey(REQUESTING_LOCATION_UPDATE)) {
            requestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATE)
        }
    }

    private fun initializeLocationService() {
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = INTERVAL
        locationRequest.fastestInterval = INTERVAL
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private val isPermissionEnabled: Boolean
        get() = ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun checkPermissions() {
        permissionListener = object : PermissionListener {
            override fun onPrivacyReady() {
                if (isLocationEnabled) {
                    onPermissionsReady()
                } else {
                    askLocationSensor()
                }
            }

            override fun onSensorReady() {
                if (isPermissionEnabled) {
                    onPermissionsReady()
                } else {
                    askPermission()
                }
            }

            override fun onPermissionRejected(missingPermission: MissingPermission?) {
                onPermissionError(missingPermission)
            }
        }
        if (!isPermissionEnabled) {
            askPermission()
        } else if (!isLocationEnabled) {
            askLocationSensor()
        } else {
            onPermissionsReady()
        }
    }

    fun localize() {
        startLocationUpdates()
    }

    fun startLocationUpdates() {
        permissionListener = object : PermissionListener {
            override fun onPrivacyReady() {
                if (isLocationEnabled) {
                    startLocationUpdates()
                } else {
                    askLocationSensor()
                }
            }

            override fun onSensorReady() {
                if (isPermissionEnabled) {
                    startLocationUpdates()
                } else {
                    askPermission()
                }
            }

            override fun onPermissionRejected(missingPermission: MissingPermission?) {
                onPermissionError(missingPermission)
            }
        }
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askPermission()
        } else if (!isLocationEnabled) {
            askLocationSensor()
        } else {
            requestingLocationUpdates = true
            fusedLocationProviderClient!!.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        stopLocationUpdates()
        super.onPause()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATE, requestingLocationUpdates)
        super.onSaveInstanceState(outState)
    }

    private val isLocationEnabled: Boolean
        get() {
            val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

    private fun askPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun askLocationSensor() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val task = LocationServices
            .getSettingsClient(requireActivity())
            .checkLocationSettings(builder.build())
        task.addOnFailureListener(requireActivity()) { e: Exception ->
            when ((e as ApiException).statusCode) {
                CommonStatusCodes.RESOLUTION_REQUIRED -> {
                    val resolvable = e as ResolvableApiException
                    sensorPermissionLauncher.launch(
                        IntentSenderRequest.Builder(resolvable.resolution.intentSender).build()
                    )
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                }
            }
        }
    }

    companion object {
        private const val INTERVAL: Long = 2000
        private const val REQUESTING_LOCATION_UPDATE = "requesting-location-update"
    }
}