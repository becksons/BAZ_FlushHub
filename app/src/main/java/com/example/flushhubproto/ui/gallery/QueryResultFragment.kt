package com.example.flushhubproto.ui.gallery

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flushhubproto.LocationInfoAdapter
import com.example.flushhubproto.MainActivity
import com.example.flushhubproto.ui.home.BathroomViewModel
import com.example.flushhubproto.ui.home.HomeFragment
import com.example.tomtom.R
import com.example.tomtom.databinding.QueryResFragmentBinding
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
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

class QueryResultFragment: Fragment() {
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


    private var _binding: QueryResFragmentBinding? = null
    private lateinit var bathroomViewModel: BathroomViewModel
    private val binding get() = _binding!!

    private var androidLocationProvider: LocationProvider? = null

    private lateinit var adapter: LocationInfoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        bathroomViewModel.selectedLocation.observe(viewLifecycleOwner) { details ->
//            binding.detailsTextView.text = details
//        }

        //setting up new instances of the same stuffs we did in HomeFragment/MainActivity
        androidLocationProvider = AndroidLocationProvider(
            context = requireContext(),
            config = androidLocationProviderConfig
        )
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = QueryResFragmentBinding.inflate(inflater, container, false)
        bathroomViewModel = ViewModelProvider(requireActivity())[BathroomViewModel::class.java]
        requestPermissionsIfNecessary()

        val queryRes = bathroomViewModel.searchQuery.value?.second
        filterMap(queryRes.toString())

        setupRecyclerView(binding)
        observeQueriedBathrooms()
        //setupRecyclerView()
        return binding.root
    }
    private fun setupRecyclerView(binding: QueryResFragmentBinding) {
        adapter = LocationInfoAdapter(emptyList())
        binding.queryResRecyclerView.queryResRecyclerList.layoutManager = LinearLayoutManager(context)
        binding.queryResRecyclerView.queryResRecyclerList.adapter = adapter
    }
    private fun observeQueriedBathrooms() {
        bathroomViewModel.queriedBathrooms.observe(viewLifecycleOwner) { bathrooms ->
            // Update adapter data
            Log.d("Observing queried bathrooms","Updating data")
            if(bathrooms!=null){
                adapter.updateData(bathrooms)
            }
        }
    }
    //filters to get only the bathrooms with specifications user want
    private fun filterMap(area: String = "all", rating: Double = 0.0){
        Log.d("Find Button call from query res frag","filter map called...")

        bathroomViewModel.queriedBathrooms.observe(viewLifecycleOwner) { dataList ->
            val processedAddresses = mutableSetOf<String>()

            dataList?.forEach { data ->
                val parts = data.first.Coordinates.split(',')
                val longitude: Double = parts[0].toDouble()
                val latitude: Double = parts[1].toDouble()
                val address: String = data.first.Location
                var distance = "N/A" //default value
                var time = "N/A" //default value
                var stars = 0.0 //default value

                //if the distance is gotten, then it implies other fields are also gotten
                if (data.second != -1.0){
                    distance = metersToMiles(data.second)
                    time = data.third.toString()
                    stars = data.first.Rating
                }

                if (address !in processedAddresses) {
                    processedAddresses.add(address)
                    mapFragment.getMapAsync{tomtomMap ->
                        if(area == "west"){
                            //-71.110940 is the line we drew to mark the start of west campus
                            if(longitude < -71.110940 && stars >= rating){
                                Log.d("remove", longitude.toString())
                                markMap(tomtomMap, latitude, longitude, address, distance, time, stars.toString())
                            }
                        }else if(area == "central"){
                            Log.d("remove", "central")
                            //central campus in between the two lines
                            if(longitude >= -71.110940 && longitude <= -71.100546 && stars >= rating){
                                markMap(tomtomMap, latitude, longitude, address, distance, time, stars.toString())
                            }
                        }else if(area == "east"){
                            Log.d("remove", "east")
                            //-71.100546 is the line we drew to mark the start of east campus
                            if(longitude > -71.100546 && stars >= rating){
                                markMap(tomtomMap, latitude, longitude, address, distance, time, stars.toString())
                            }
                        }else{
                            Log.d("remove", "all")
                            markMap(tomtomMap, latitude, longitude, address, distance, time, stars.toString())
                        }
                    }
                }
            }
        }
        mapFragment.getMapAsync{tomtomMap ->
            tomtomMap.addMarkerClickListener { clickedMarker ->
                val detailText = clickedMarker.tag

                if (detailText != null) {
                    bathroomViewModel.updateSelectedLocation(detailText)
                }

                showGoToRouteLayout(clickedMarker.coordinate.latitude, clickedMarker.coordinate.longitude, clickedMarker.tag!!)
            }
        }
    }

    private fun requestPermissionsIfNecessary() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                HomeFragment.REQUEST_LOCATION_PERMISSION
            )
        } else {
            initializeMapWithLocation()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            HomeFragment.REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeMapWithLocation()
                }
            }
        }
    }

    private val mapOptions = MapOptions(mapKey ="YbAIKDlzANgswfBTirAdDONIKfLN9n6J")
    private val mapFragment = MapFragment.newInstance(mapOptions)
    private val queryList = mutableListOf<Marker>()
    private fun initializeMapWithLocation() {
        childFragmentManager.beginTransaction()
            .replace(R.id.query_map_container, mapFragment)
            .commit()

        mapFragment.getMapAsync { tomtomMap ->
            tomtomMap.setLocationProvider(androidLocationProvider)
            androidLocationProvider?.enable()

            val onLocationUpdateListener = OnLocationUpdateListener { location: GeoLocation ->
                Log.d("Location Update", "Latitude: ${location.position.latitude}, Longitude: ${location.position.longitude}")

                MainActivity.currentLatitude = location.position.latitude
                MainActivity.currentLongitude = location.position.longitude

                moveMap(tomtomMap, location.position.latitude, location.position.longitude)
                updateUserLocationOnMap(tomtomMap,location.position.latitude,location.position.longitude)
            }

            androidLocationProvider?.addOnLocationUpdateListener(onLocationUpdateListener)
        }
    }

    //Converting meters to miles
    private fun metersToMiles(meters: Double): String {
        val conversionFactor = 0.000621371
        return String.format("%.1f", meters * conversionFactor)
    }

    //This function adds markers to the TomTomMap API fragment
    private fun markMap(tomtomMap: TomTomMap, lat: Double, long: Double, address: String = "Bathroom", distance: String = "0", eta: String = "0", rating: String = "0.0") {
        val loc = GeoPoint(lat, long)
        val markerOptions = MarkerOptions(
            coordinate = loc,
            pinImage = ImageFactory.fromResource(R.drawable.bathroom_location_icon),
            tag = "Address: ${address}\n" +
                    "Distance: $distance" + requireContext().getString(R.string.miles) +
                    "\nETA: $eta" + requireContext().getString(R.string.minutes) +
                    "\nRating: $rating" + requireContext().getString(R.string.stars)
        )

        val marker = tomtomMap.addMarker(markerOptions)
        queryList.add(marker)
        //-----------------------------

        tomtomMap.addMarkerClickListener { clickedMarker ->
            val detailText = clickedMarker.tag

            if (detailText != null) {
                bathroomViewModel.updateSelectedLocation(detailText)
            }


            Log.d(" Query MarkerClick", "Marker at $address was clicked.")
            showGoToRouteLayout(clickedMarker.coordinate.latitude, clickedMarker.coordinate.longitude, clickedMarker.tag!!, )
        }
    }

    //This function reveals a layout for the user to launch Google Maps to route them
    private fun showGoToRouteLayout(lat:Double, lon: Double, tagData: String = "Bathroom") {
        val tagDataList = tagData.split("\n")
        val address  = tagDataList[0]
        val distance = tagDataList[1]
        val eta = tagDataList[2]
        val rating = tagDataList[3]

        binding.goToRouteLayout.root.visibility = View.VISIBLE
        binding.goToRouteLayout.root.apply {
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(null)
        }


        binding.goToRouteLayout.showRouteLayoutAddress.text = address
        binding.goToRouteLayout.showRouteLayoutDistance.text = distance
        binding.goToRouteLayout.showRouteLayoutEta.text = eta
        binding.goToRouteLayout.showRouteLayoutRatingBar.progress = rating.toDouble().toInt()
        binding.goToRouteLayout.showRouteLayoutRatingBar.isClickable = false

        binding.goToRouteLayout.collapseButton.setOnClickListener {
            hideGoToRouteLayout()
        }

        bathroomViewModel.updateSelectedLocation(address)
    }
    private fun hideGoToRouteLayout() {
        binding.goToRouteLayout.root.visibility = View.GONE
    }

    //This function passes lat and long to Google maps and launch it to route the user
    fun openMap(context: Context, lat: Double, long: Double, label: String = "Restroom") {
        val geoUri = android.net.Uri.parse("geo:0,0?q=$lat,$long($label)")
        val intent = Intent(Intent.ACTION_VIEW, geoUri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val webUri = android.net.Uri.parse("https://www.google.com/maps/@$lat,$long,16z")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            context.startActivity(webIntent)
        }
    }


    private fun updateUserLocationOnMap(tomtomMap: TomTomMap, lat: Double, long: Double) {
        val customArrowImage = ImageFactory.fromResource(R.drawable.nav_arrow)
        val file = File("/Users/becksonstein/AndroidStudioProjects/FlushHubProto/app/src/main/assets/custom_nav_arrow.svg")

        val locationMarkerOptions = LocationMarkerOptions(
            type = LocationMarkerOptions.Type.Chevron
        )

        tomtomMap.enableLocationMarker(locationMarkerOptions)
    }

    //Move the map to the current user location
    private fun moveMap(tomtomMap: TomTomMap, lat: Double, long: Double){
        val cameraOptions = CameraOptions(
            position = GeoPoint(lat, long),
            zoom = 17.0,
            tilt = 0.0,
            rotation = 0.0
        )

        tomtomMap.moveCamera(cameraOptions)
    }

    private val androidLocationProviderConfig = AndroidLocationProviderConfig(
        minTimeInterval = 250L.milliseconds,
        minDistance = Distance.meters(20.0)
    )
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        androidLocationProvider = null
    }
}