package com.example.flushhubproto.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.flushhubproto.LocationInfoAdapter
import com.example.flushhubproto.MainActivity
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
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import kotlin.time.Duration.Companion.milliseconds

interface RouteActionListener {
    fun onRouteClosed()
}
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
    }


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LocationInfoAdapter

    private var isExpanded = false

    private lateinit var bathroomViewModel: BathroomViewModel
    private var isBarVisible: Boolean = false
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    var androidLocationProvider: LocationProvider? = null

    private val mapOptions = MapOptions(mapKey ="YbAIKDlzANgswfBTirAdDONIKfLN9n6J")
    val mapFragment = MapFragment.newInstance(mapOptions)

    private val markerOptionsList: MutableList<MarkerOptions> = mutableListOf()
    private var markerTags: MutableList<String> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fun toggleBar() {
            isBarVisible = !isBarVisible
        }
        bathroomViewModel.selectedLocation.observe(viewLifecycleOwner) { details ->
            binding.detailsTextView.text = details
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreate(savedInstanceState)
        Log.d("DEBUG", "CURRENT LONGITUDE: ${MainActivity.currentLongitude}, CURRENT LATITUDE: ${MainActivity.currentLatitude}")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        bathroomViewModel = ViewModelProvider(requireActivity())[BathroomViewModel::class.java]
        childFragmentManager.beginTransaction()
        .replace(R.id.map_container, mapFragment)
        .commit()

        checkGPS()
        addMarkers()
        setupRecyclerView(binding)
        observeLocationInfos()
        return binding.root
    }

    //Converting meters to miles
    private fun metersToMiles(meters: Double): String {
        val conversionFactor = 0.000621371
        return String.format("%.1f", meters * conversionFactor)
    }


    private fun setupRecyclerView(binding: FragmentHomeBinding) {
        swipeRefreshLayout = binding.nearestRestroomSwipeRefreshLayout
        recyclerView = binding.nearestLocationRecyclerView.nearestLocationRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = LocationInfoAdapter(emptyList())
        recyclerView.adapter = adapter
        //Nearest location review swipe refresh
        swipeRefreshLayout.setOnRefreshListener {
            bathroomViewModel.loadAllBathrooms()
            Toast.makeText(context, "View refreshed", Toast.LENGTH_SHORT).show()
            swipeRefreshLayout.isRefreshing = (MainActivity.swipeLoading.value == true)
        }
    }

    private fun checkGPS() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            initializeMapWithoutLocation()
        } else {
            initializeMapWithLocation()
        }
    }

    private fun initializeMapWithoutLocation() {
        mapFragment.getMapAsync { tomtomMap ->
            moveMap(tomtomMap, MainActivity.currentLatitude, MainActivity.currentLongitude)
            val loc = GeoPoint(MainActivity.currentLatitude, MainActivity.currentLongitude) //converting lat and long to GeoPoint type
            val markerOptions = MarkerOptions( //assigning informations of user marker which will not move
                coordinate = loc,
                pinImage = ImageFactory.fromResource(R.drawable.map_default_pin)
            )

            tomtomMap.addMarker(markerOptions)

            tomtomMap.addMapClickListener {
                hideGoToRouteLayout()
                return@addMapClickListener true
            }
        }
    }
    private fun initializeMapWithLocation() {
        mapFragment.getMapAsync { tomtomMap ->
            androidLocationProvider = AndroidLocationProvider(
                context = requireContext(),
                config = androidLocationProviderConfig
            )
            tomtomMap.setLocationProvider(androidLocationProvider)
            androidLocationProvider?.enable()

            Log.d("INIT", "Setting TOM TOM Map...")

            val onLocationUpdateListener = OnLocationUpdateListener { location: GeoLocation ->
                Log.d("Location Update", "Latitude: ${location.position.latitude}, Longitude: ${location.position.longitude}")

                MainActivity.currentLatitude = location.position.latitude
                MainActivity.currentLongitude = location.position.longitude

                moveMap(tomtomMap, location.position.latitude, location.position.longitude)
                updateUserLocationOnMap(tomtomMap,location.position.latitude,location.position.longitude)
            }

            tomtomMap.addMapClickListener {
                hideGoToRouteLayout()
                return@addMapClickListener true
            }

            androidLocationProvider?.addOnLocationUpdateListener(onLocationUpdateListener)
        }
    }

    private val androidLocationProviderConfig = AndroidLocationProviderConfig(
        minTimeInterval = 1000.milliseconds,
        minDistance = Distance.meters(10.0)
    )

    private fun moveMap(tomtomMap: TomTomMap, lat: Double, long: Double){
        val cameraOptions = CameraOptions(
            position = GeoPoint(lat, long),
            zoom = 17.0,
            tilt = 0.0,
            rotation = 0.0
        )

        tomtomMap.moveCamera(cameraOptions)
    }

    private fun updateUserLocationOnMap(tomtomMap: TomTomMap, lat: Double, long: Double) {
        val locationMarkerOptions = LocationMarkerOptions(
            type = LocationMarkerOptions.Type.Chevron
        )

        tomtomMap.enableLocationMarker(locationMarkerOptions)
    }

    private fun observeLocationInfos() {
        Log.d("Home recycler length", "${bathroomViewModel.bathrooms.value?.size}")
        bathroomViewModel.bathrooms.observe(viewLifecycleOwner) { bathrooms ->
            if (bathrooms != null) {
                adapter.updateData(bathrooms)
            }
        }
    }


    //This function passes lat and long to Google maps and launch it to route the user
    fun openMap(context: Context, lat: Double, long: Double, label: String = "Restroom") {
        val geoUri = android.net.Uri.parse("geo:0,0?q=$lat,$long($label)") //lat, long, and what you want to call the location
        val intent = Intent(Intent.ACTION_VIEW, geoUri) //using intent
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val webUri = android.net.Uri.parse("https://www.google.com/maps/@$lat,$long,16z")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            context.startActivity(webIntent)
        }
    }

    private fun addMarkers(){
        bathroomViewModel.bathrooms.observe(viewLifecycleOwner) { dataList ->
            val processedAddresses = mutableSetOf<String>()

            //Converting the bathrooms data from DB to makers for the map
            dataList?.forEach { data ->
                val parts = data.first.Coordinates.split(',')
                val longitude: Double = parts[0].toDouble()
                val latitude: Double = parts[1].toDouble()
                val address: String = data.first.Location
                var distance = "N/A"
                var time = "N/A"
                var stars = "N/A"

                if (data.second != -1.0){
                    distance = metersToMiles(data.second)
                    time = data.third.toString()
                    stars = data.first.Rating.toString()
                }

                if (address !in processedAddresses) {
                    processedAddresses.add(address)
                    makeMarker(latitude, longitude, address, distance, time, stars)
                }
            }

            mapFragment.getMapAsync { tomtomMap ->
                markMap(tomtomMap)
            }
        }
    }

    //This function adds markers to the TomTomMap API fragment
    private fun makeMarker(lat: Double, long: Double, address: String = "Bathroom", distance: String = "0", eta: String = "0", rating: String = "0.0") {
        val loc = GeoPoint(lat, long) //converting lat and long to GeoPoint type
        val markerOptions = MarkerOptions( //assigning informations of this marker
            coordinate = loc,
            pinImage = ImageFactory.fromResource(R.drawable.bathroom_location_icon),
            tag = "Address: ${address}\n" +
                    "Distance: $distance" + " " +  requireContext().getString(R.string.miles) +
                    "\nETA: $eta" + " " + requireContext().getString(R.string.minutes) +
                    "\nRating: $rating" + " " + requireContext().getString(R.string.stars)
        )

        markerTags.add(markerOptions.tag.toString())
        markerOptionsList.add(markerOptions)
    }

    private fun markMap(tomtomMap: TomTomMap){
        tomtomMap.addMarkers(markerOptionsList)

        //making the marker clickable
        tomtomMap.addMarkerClickListener { clickedMarker ->
            val detailText = clickedMarker.tag
            if (detailText != null) {
                bathroomViewModel.updateSelectedLocation(detailText)
            }

            Log.d("MarkerClick", clickedMarker.id.toString())

            //show a UI if click
            showGoToRouteLayout(clickedMarker.coordinate.latitude, clickedMarker.coordinate.longitude, clickedMarker.tag!!)
        }
    }

    private fun removerAllMarkers(){
        mapFragment.getMapAsync { tomtomMap ->
            markerTags.forEach {tag ->
                tomtomMap.removeMarkers(tag)
                //tomtomMap.removeMapClickListener()
            }
        }

        markerTags.clear()
    }

    //This function reveals a layout for the user to launch Google Maps to route them
    private fun showGoToRouteLayout(lat:Double, lon: Double, address: String = "Bathroom") {
        binding.goToRouteLayout.visibility = VISIBLE
        binding.goToRouteLayout.apply {
            visibility = View.VISIBLE
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(null)
        }

        binding.detailsTextView.text = null
        binding.detailsTextView.text = address
        binding.mapButton.setOnClickListener {
            context?.let{ctx->
                openMap(ctx, lat, lon, address)
            }
        }

        bathroomViewModel.updateSelectedLocation(address)
    }
    private fun hideGoToRouteLayout() {
        binding.goToRouteLayout.visibility = GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removerAllMarkers()
        androidLocationProvider = null
        _binding = null
    }
}