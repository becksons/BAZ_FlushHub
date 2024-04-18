package com.example.flushhubproto.ui.home


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import kotlin.math.roundToInt
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.flushhubproto.BathroomViewModel
import com.example.flushhubproto.LocationInfoAdapter
import com.example.tomtom.R
import com.example.tomtom.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tomtom.quantity.Distance
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.android.AndroidLocationProvider
import com.tomtom.sdk.location.android.AndroidLocationProviderConfig
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import kotlin.time.Duration.Companion.milliseconds


class HomeFragment : Fragment() {
    data class RouteResponse(
        val formatVersion: String,
        val routes: List<Route>
    )

    data class Route(
        val summary: RouteSummary,
        val legs: List<Leg>
    )

    data class RouteSummary(
        val lengthInMeters: Int,
        val travelTimeInSeconds: Int,
        val departureTime: String,
        val arrivalTime: String
    )

    data class Leg(
        val summary: RouteSummary,
        val points: List<Point>
    )

    data class Point(
        val latitude: Double,
        val longitude: Double
    )


    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1

        private val bottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) { /* Handle state change */ }
                override fun onSlide(bottomSheet: View, slideOffset: Float) { /* Handle sliding */ }
        }

        var currentLongitude: Double = 0.0
        var currentLatitude: Double = 0.0
    }


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var androidLocationProvider: LocationProvider? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LocationInfoAdapter
    private lateinit var viewModel: HomeViewModel
    private var isExpanded = false

    private val bathroomViewModel: BathroomViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        androidLocationProvider = AndroidLocationProvider(
            context = requireContext(),
            config = androidLocationProviderConfig
        )
    }         // Now can use androidLocationProvider safely within fragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        requestPermissionsIfNecessary()

        // Load the Markers into the Tom Tom Map.
        bathroomViewModel.bathrooms.observe(viewLifecycleOwner) { dataList ->
            // Unpack the test object and get the long-lat coordinates
            dataList?.forEach { data ->
                val parts = data.Coordinates.split(',')
                val longitude: Double = parts[0].toDouble()
                val latitude: Double = parts[1].toDouble()

                mapFragment.getMapAsync { tomtomMap ->
                    markMap(tomtomMap,latitude,longitude)
                }
            }
        }

//        context?.let { ctx ->
//            openMap(ctx,42.350026020986256, -71.10326632227299) //parsing to Google Maps
//        }
//

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        setupRecyclerView(binding)

        observeLocationInfos()

        return binding.root
    }


    private fun setupRecyclerView(binding: FragmentHomeBinding) {
        recyclerView = binding.nearestLocationRecyclerView.nearestLocationRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = LocationInfoAdapter(emptyList())
        recyclerView.adapter = adapter
    }


    private fun observeLocationInfos() {
        bathroomViewModel.bathrooms.observe(viewLifecycleOwner) { bathrooms ->
            if (bathrooms != null) {
                adapter.updateData(bathrooms)
            }
        }
    }


    private fun requestPermissionsIfNecessary() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            initializeMapWithLocation()
        }
    }

    @Deprecated("Deprecated in Java")
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

    private val androidLocationProviderConfig = AndroidLocationProviderConfig(
        minTimeInterval = 250L.milliseconds,
        minDistance = Distance.meters(20.0)
    )



    private fun initializeMapWithLocation() {
        childFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        mapFragment.getMapAsync { tomtomMap ->
            tomtomMap.setLocationProvider(androidLocationProvider)
            androidLocationProvider?.enable()

            val onLocationUpdateListener = OnLocationUpdateListener { location: GeoLocation ->
                Log.d("Location Update", "Latitude: ${location.position.latitude}, Longitude: ${location.position.longitude}")

                // Store the Current Latitude and Longitude
                currentLatitude = location.position.latitude
                currentLongitude = location.position.longitude

                // Move map to the new location
                moveMap(tomtomMap, location.position.latitude, location.position.longitude)
                updateUserLocationOnMap(tomtomMap,location.position.latitude,location.position.longitude)
                //calRange(location.position.latitude, location.position.longitude, 42.350026020986256, -71.10326632227299)
            }

            androidLocationProvider?.addOnLocationUpdateListener(onLocationUpdateListener)
        }
    }
    private fun updateUserLocationOnMap(tomtomMap: TomTomMap, lat: Double, long: Double) {

        val locationMarkerOptions = LocationMarkerOptions(
            type = LocationMarkerOptions.Type.Pointer
        )

        tomtomMap.enableLocationMarker(locationMarkerOptions)
    }

    private fun moveMap(tomtomMap: TomTomMap, lat: Double, long: Double){
        val cameraOptions = CameraOptions(
            position = GeoPoint(lat, long),
            zoom = 17.0,
            tilt = 0.0,
            rotation = 0.0
        )

        tomtomMap.moveCamera(cameraOptions)
    }
    fun openMap(context: Context, lat: Double, long: Double, label: String = "Restroom") {
        val geoUri = Uri.parse("geo:0,0?q=$lat,$long($label)")
        val intent = Intent(Intent.ACTION_VIEW, geoUri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val webUri = Uri.parse("https://www.google.com/maps/@$lat,$long,16z")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            context.startActivity(webIntent)
        }
    }

    private fun markMap(tomtomMap: TomTomMap, lat: Double, long: Double){
        val loc = GeoPoint(lat, long)
        val markerOptions = MarkerOptions(
            coordinate = loc,
            pinImage = ImageFactory.fromResource(R.drawable.bathroom_location_icon)
        )

        tomtomMap.addMarker(markerOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        androidLocationProvider = null

    }
}