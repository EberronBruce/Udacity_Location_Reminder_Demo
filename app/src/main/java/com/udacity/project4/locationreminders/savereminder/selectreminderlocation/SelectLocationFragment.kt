package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.lang.Exception
import java.util.*


private val TAG = SelectLocationFragment::class.java.simpleName
//private const val REQUEST_LOCATION_PERMISSION = 100

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    var location: Location? = null
    private val zoomControlOffset = 50
    private lateinit var userLocation: LatLng
    private lateinit var marker: Marker

    private lateinit var selectedPoi: PointOfInterest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        userLocation = LatLng(37.422131, -122.084801)

        location = Location(activity as AppCompatActivity, object: locationListener {
            override fun locationResponse(locationResult: LocationResult) {
                //Log.d(TAG, "Location Response: $locationResult")
                val currentUserLocation = LatLng(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)
                if (currentUserLocation != userLocation) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 18f))
                    userLocation = currentUserLocation
                }

            }
        })
//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        binding.saveButton.setOnClickListener {
            onLocationSelected()
        }


        binding.saveButton.visibility = View.INVISIBLE

        return binding.root
    }

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap == null) return
        map = googleMap
        setMapLongClick(map)
        setPoiClick(map)

        enableMyLocation()
    }

    private fun setMapLongClick(map: GoogleMap) {

        map.setOnMapLongClickListener { latLng ->
            map.clear()

            val snippet = String.format(
                Locale.getDefault(),
                "Latitude: %1$.5f, Longitude: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            Log.d(TAG, "Set Long Click")

            selectedPoi = PointOfInterest(latLng, snippet, snippet)

            marker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Reminder Location")
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
            binding.saveButton.visibility = View.VISIBLE
        }
    }

    private fun setPoiClick(map: GoogleMap) {

        map.setOnPoiClickListener { poi ->
            Log.d(TAG, "${poi.name} ${poi.placeId}")
            map.clear()
            selectedPoi = poi
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
            binding.saveButton.visibility = View.VISIBLE
        }
    }


    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            @SuppressLint("MissingPermission")
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = false

        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        location?.onRequestPermissionResult(requestCode,permissions,grantResults)

    }

    override fun onStart() {
        super.onStart()
        location?.initializeLocation()
    }

    override fun onStop() {
        super.onStop()
        location?.stopUdateLocation()
    }

}
