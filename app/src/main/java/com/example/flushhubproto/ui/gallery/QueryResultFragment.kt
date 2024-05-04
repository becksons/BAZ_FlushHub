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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.flushhubproto.LocationInfoAdapter
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
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var adapter: LocationInfoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        bathroomViewModel.filterCriteria.observe(viewLifecycleOwner) { criteria ->
//            filterMap(criteria)
//        }

        bathroomViewModel.selectedLocation.observe(viewLifecycleOwner) { details ->
            binding.detailsTextView.text = details
        }

        androidLocationProvider = AndroidLocationProvider(
            context = requireContext(),
            config = androidLocationProviderConfig
        )
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = QueryResFragmentBinding.inflate(inflater, container, false)
        bathroomViewModel = ViewModelProvider(requireActivity())[BathroomViewModel::class.java]

        val queryRes = bathroomViewModel.searchQuery.value?.second
        filterMap(queryRes.toString())
        swipeRefreshLayout = binding.queryRecyclerListSwipeRefresh
        swipeRefreshLayout.setOnRefreshListener {

            Toast.makeText(context, "View refreshed", Toast.LENGTH_SHORT).show()
            swipeRefreshLayout.isRefreshing = false
        }

        setupRecyclerView(binding)
        observeQueriedBathrooms()
        //setupRecyclerView()
        requestPermissionsIfNecessary()


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
                Log.d("Observing queried bathrooms","Data not null...")

                adapter.updateData(bathrooms)
            }
        }
    }




    //    private fun observeLocationInfos() {
//        bathroomViewModel.bathrooms.observe(viewLifecycleOwner) { bathrooms ->
//            if (bathrooms != null) {
//                adapter.updateData(bathrooms)
//            }
//        }
//    }
    private fun filterMap(area: String = "all", rating: Double = 0.0){
        Log.d("Find Button call from query res frag","filter map called...")

        bathroomViewModel.bathrooms.observe(viewLifecycleOwner) { dataList ->
            val processedAddresses = mutableSetOf<String>()

            dataList?.forEach { data ->
                val parts = data.first.Coordinates.split(',')
                val longitude: Double = parts[0].toDouble()
                val latitude: Double = parts[1].toDouble()
                val address: String = data.first.Location
                var distance = "N/A"
                var time = "N/A"
                var stars = 0.0

                if (data.second != -1.0){
                    distance = metersToMiles(data.second)
                    time = data.third.toString()
                    stars = data.first.Rating
                }

                if (address !in processedAddresses) {
                    processedAddresses.add(address)
                    mapFragment.getMapAsync{tomtomMap ->
                        if(area == "west"){
                            if(longitude < -71.110940 && stars >= rating){
                                Log.d("remove", longitude.toString())
                                markMap(tomtomMap, latitude, longitude, address, distance, time, stars.toString())
                            }
                        }else if(area == "central"){
                            Log.d("remove", "central")
                            if(longitude >= -71.110940 && longitude <= -71.100546 && stars >= rating){
                                markMap(tomtomMap, latitude, longitude, address, distance, time, stars.toString())
                            }
                        }else if(area == "east"){
                            Log.d("remove", "east")
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
        when (requestCode) {
            HomeFragment.REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeMapWithLocation()
                } else {
                    //TODO: error handle for permission denied
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

                HomeFragment.currentLatitude = location.position.latitude
                HomeFragment.currentLongitude = location.position.longitude

                moveMap(tomtomMap, location.position.latitude, location.position.longitude)
                updateUserLocationOnMap(tomtomMap,location.position.latitude,location.position.longitude)
            }

            androidLocationProvider?.addOnLocationUpdateListener(onLocationUpdateListener)
        }
    }
    private fun metersToMiles(meters: Double): String {
        val conversionFactor = 0.000621371
        return String.format("%.1f", meters * conversionFactor)
    }
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
    private fun showGoToRouteLayout(lat:Double, lon: Double, address: String = "Bathroom") {
        val layout = binding.goToRouteLayout
        binding.goToRouteLayout.visibility = View.VISIBLE
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
//            customModel = android.net.Uri.fromFile(file)
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