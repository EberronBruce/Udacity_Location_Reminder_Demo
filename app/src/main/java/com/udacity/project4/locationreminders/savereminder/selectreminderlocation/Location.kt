package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

const val REQUEST_CODE_LOCATION = 100

interface locationListener {
    fun locationResponse(locationResult: LocationResult)
}

private val TAG = Location::class.java.simpleName
class Location(var activity: AppCompatActivity, locationListener: locationListener) {
    private val permissionFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permissionCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION

    //private val REQUEST_CODE_LOCATION = 100

    private var fusedLocationClient: FusedLocationProviderClient? = null

    private var locationRequest: LocationRequest? = null
    private var callback: LocationCallback? = null

    init {
        fusedLocationClient = FusedLocationProviderClient(activity.applicationContext)
        initializeLocationRequest()
        callback = object: LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
                if (p0 != null) {
                    locationListener.locationResponse(p0)
                } else {
                    Log.e(TAG, "Location Result is null.")
                }
            }
        }

    }

    private fun initializeLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.interval = 50000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun validatePermissionsLocation():Boolean {
        val fineLocationAvailable = ActivityCompat.checkSelfPermission(activity.applicationContext, permissionFineLocation) == PackageManager.PERMISSION_GRANTED
        val coarseLocationAvailable = ActivityCompat.checkSelfPermission(activity.applicationContext, permissionCoarseLocation) == PackageManager.PERMISSION_GRANTED

        return fineLocationAvailable && coarseLocationAvailable
    }

    private fun requestPermissions() {
        val contextProvider = ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionFineLocation)

        if (contextProvider) {
            Toast.makeText(activity.applicationContext, "Permission is required to obtain location", Toast.LENGTH_LONG).show()
        }
        permissionRequest()
    }

    private fun permissionRequest() {
        ActivityCompat.requestPermissions(activity, arrayOf(permissionFineLocation, permissionCoarseLocation), REQUEST_CODE_LOCATION)
    }

    fun onRequestPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "Permissions: $permissions")
        when(requestCode) {
            REQUEST_CODE_LOCATION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation()
                } else {
                    Toast.makeText(activity.applicationContext, "You did not give permissions to get location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun stopUpdateLocation() {
        this.fusedLocationClient?.removeLocationUpdates(callback)
    }

    fun initializeLocation() {
        if (validatePermissionsLocation()) {
            getLocation()
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        validatePermissionsLocation()
        fusedLocationClient?.requestLocationUpdates(locationRequest, callback, null)
    }
}