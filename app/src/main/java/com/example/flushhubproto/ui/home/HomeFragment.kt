
package com.example.flushhubproto.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.example.flushhubproto.LocationInfoAdapter
import com.example.tomtom.R
import com.example.tomtom.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.tomtom.quantity.Distance
import com.tomtom.sdk.common.Callback
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.location.android.AndroidLocationProvider
import com.tomtom.sdk.location.android.AndroidLocationProviderConfig
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.UiComponentClickListener
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.ItineraryPoint
import com.tomtom.sdk.routing.options.RangeCalculationOptions
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.range.Budget
import com.tomtom.sdk.routing.range.Range
import com.tomtom.sdk.routing.range.RangeCalculator
import com.tomtom.sdk.vehicle.Vehicle
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class HomeFragment : Fragment() {

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var androidLocationProvider: LocationProvider? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        androidLocationProvider = AndroidLocationProvider(
            context = requireContext(),
            config = androidLocationProviderConfig
        )
        // Now can use androidLocationProvider safely within fragment
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        requestPermissionsIfNecessary()

        return binding.root
    }


    private fun requestPermissionsIfNecessary() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {

            initializeMapWithLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeMapWithLocation()
                } else {
                    // Handle deny permission
                }
            }
        }
    }

    private val mapOptions = MapOptions(mapKey ="AOYMhs1HWBhlfnU4mIaiSULFfvNGTw4Z")
    private val mapFragment = MapFragment.newInstance(mapOptions)

    val routePlanner = OnlineRoutePlanner.create(requireContext(), "AOYMhs1HWBhlfnU4mIaiSULFfvNGTw4Z")

    val androidLocationProviderConfig = AndroidLocationProviderConfig(
        minTimeInterval = 250L.milliseconds,
        minDistance = Distance.meters(20.0)
    )

//    val androidLocationProvider: LocationProvider = AndroidLocationProvider(
//        context = requireContext(),
//        config = androidLocationProviderConfig
//    )


    private fun initializeMapWithLocation() {

        childFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        mapFragment.getMapAsync { tomtomMap ->
            tomtomMap.setLocationProvider(androidLocationProvider)
            androidLocationProvider?.enable()

            val onLocationUpdateListener = OnLocationUpdateListener { location: GeoLocation ->
                Log.d("Location Update", "Latitude: ${location.position.latitude}, Longitude: ${location.position.longitude}")

                // Move map to the new location
                moveMap(tomtomMap, location.position.latitude, location.position.longitude)
                updateUserLocationOnMap(tomtomMap, location.position.latitude, location.position.longitude)
            }

            // Register the listener with the location provider
            androidLocationProvider?.addOnLocationUpdateListener(onLocationUpdateListener)
        }
    }

    private fun updateUserLocationOnMap(tomtomMap: TomTomMap, lat: Double, long: Double) {

        val locationMarkerOptions = LocationMarkerOptions(
            type = LocationMarkerOptions.Type.Pointer
        )

        tomtomMap.enableLocationMarker(locationMarkerOptions)
    }
    fun moveMap(tomtomMap: TomTomMap,lat: Double, long: Double){
        val cameraOptions = CameraOptions(
            position = GeoPoint(lat, long),
            zoom = 17.0,
            tilt = 0.0,
            rotation = 0.0
        )

        tomtomMap.moveCamera(cameraOptions)
    }

    fun markMap(tomtomMap: TomTomMap, lat: Double, long: Double){
        val loc = GeoPoint(lat, long)
        val markerOptions = MarkerOptions(
            coordinate = loc,
            pinImage = ImageFactory.fromResource(R.drawable.bathroom_location_icon)
        )

        tomtomMap.addMarker(markerOptions)
    }



//    fun calRange(tomtomMap: TomTomMap, startLat: Double, startLong: Double, desLat: Double, desLong: Double){
//
//        val des = ItineraryPoint(Place(GeoPoint(52.377956, 4.897070)))
//
//        val vehicle = Vehicle.Pedestrian()
//
//        val rangeCalculator = RangeCalculator()
//
//        var range = ""
//
//        val rangeCalculationOptions = RangeCalculationOptions(
//            origin = des,
//            budgets = setOf(Budget.Distance(Distance.meters(1000))),
//            vehicle = vehicle
//        )
//
//        when (val calculateRangeResult = rangeCalculator.calculateRange(rangeCalculationOptions)) {
//            is Result.Success -> calculateRangeResult.value()
//            is Result.Failure -> calculateRangeResult.failure()
//        }
//    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        androidLocationProvider = null
    }
}
