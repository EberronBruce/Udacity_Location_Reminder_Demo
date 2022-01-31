package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


//private val TAG = SelectLocationFragment::class.java.simpleName
//private const val REQUEST_LOCATION_PERMISSION = 100

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    var location: Location? = null
    private var mapZoom = 18f
    private lateinit var userLocation: LatLng
    private lateinit var marker: Marker

    private lateinit var selectedPoi: PointOfInterest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
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
                val currentUserLocation = LatLng(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)
                val results = FloatArray(1)
                android.location.Location.distanceBetween(userLocation.latitude, userLocation.longitude, currentUserLocation.latitude, currentUserLocation.longitude, results)
                if (results[0] > 1.0) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, map.cameraPosition.zoom))
                    userLocation = currentUserLocation
                }
            }
        })
//        TODO: add style to the map

        binding.saveButton.setOnClickListener {
            onLocationSelected()
        }

        binding.saveButton.visibility = View.INVISIBLE

        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.selectedPOI.value = selectedPoi
        _viewModel.latitude.value = selectedPoi.latLng.latitude
        _viewModel.longitude.value = selectedPoi.latLng.longitude
        _viewModel.reminderSelectedLocationStr.value = selectedPoi.name
        findNavController().popBackStack()
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
        map.animateCamera(CameraUpdateFactory.zoomTo(mapZoom))
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
        location?.stopUpdateLocation()
    }

}
