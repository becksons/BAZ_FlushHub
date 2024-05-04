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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.flushhubproto.LocationInfoAdapter
import com.example.tomtom.R
import com.example.tomtom.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
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
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.math.roundToInt
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

        var currentLongitude: Double = 0.0
        var currentLatitude: Double = 0.0
    }


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var androidLocationProvider: LocationProvider? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LocationInfoAdapter

    private var isExpanded = false

    private lateinit var bathroomViewModel: BathroomViewModel
    private var isBarVisible: Boolean = false
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fun toggleBar() {
            isBarVisible = !isBarVisible
        }

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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        bathroomViewModel = ViewModelProvider(requireActivity())[BathroomViewModel::class.java]
        requestPermissionsIfNecessary()

        bathroomViewModel.bathrooms.observe(viewLifecycleOwner) { dataList ->
            val processedAddresses = mutableSetOf<String>()

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
                    mapFragment.getMapAsync { tomtomMap ->
                        markMap(tomtomMap, latitude, longitude, address, distance, time, stars)
                    }
                }
            }
        }


        setupRecyclerView(binding)

        observeLocationInfos()

        return binding.root
    }

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

            Toast.makeText(context, "View refreshed", Toast.LENGTH_SHORT).show()
            swipeRefreshLayout.isRefreshing = false
        }
    }


    private fun observeLocationInfos() {
        Log.d("Home recycler length", "${bathroomViewModel.bathrooms.value?.size}")
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
                    //TODO: error handle for permission denied
                }
            }
        }
    }

    private val mapOptions = MapOptions(mapKey ="YbAIKDlzANgswfBTirAdDONIKfLN9n6J")
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

                currentLatitude = location.position.latitude
                currentLongitude = location.position.longitude


                moveMap(tomtomMap, location.position.latitude, location.position.longitude)
                updateUserLocationOnMap(tomtomMap,location.position.latitude,location.position.longitude)
                //calRange(location.position.latitude, location.position.longitude, 42.350026020986256, -71.10326632227299)
            }


            androidLocationProvider?.addOnLocationUpdateListener(onLocationUpdateListener)
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

        tomtomMap.addMarkerClickListener { clickedMarker ->
            val detailText = clickedMarker.tag

            if (detailText != null) {
                bathroomViewModel.updateSelectedLocation(detailText)
            }


            Log.d("MarkerClick", "Marker at $address was clicked.")
            showGoToRouteLayout(clickedMarker.coordinate.latitude, clickedMarker.coordinate.longitude, clickedMarker.tag!!, )

        }
    }


    private fun calcRange(startLat: Double, startLong: Double, desLat: Double, desLong: Double): List<Int>? {
        val url = "https://api.tomtom.com/routing/1/calculateRoute/$startLat,$startLong:$desLat,$desLong/json?key=AOYMhs1HWBhlfnU4mIaiSULFfvNGTw4Z&travelMode=pedestrian"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()
        var routeLength = 0
        var routeTime = 0

        val executor = Executors.newSingleThreadExecutor()

        val task: Callable<List<Int>> = Callable<List<Int>> {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val responseData = response.body?.string()
                if (responseData != null) {
                    val routeResponse = parseRouteData(responseData)
                    routeResponse.routes.forEach { route ->
                        routeLength = route.summary.lengthInMeters
                        routeTime = (route.summary.travelTimeInSeconds/60.0).roundToInt()
                    }
                    return@Callable listOf(routeLength, routeTime)
                }
                throw IllegalStateException("Response Data is Null!")
            }
        }

        val future = executor.submit(task)
        val results: List<Int>? = try {
            future.get()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            executor.shutdown()
        }

        return results
    }

    private fun parseRouteData(jsonData: String): HomeFragment.RouteResponse {
        val gson = Gson()
        return gson.fromJson(jsonData, HomeFragment.RouteResponse::class.java)
    }


    private fun showGoToRouteLayout(lat:Double, lon: Double, address: String = "Bathroom") {
        val layout = binding.goToRouteLayout
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


    private fun openDetailFragment(marker: Marker) {
        val lat = marker.coordinate.latitude
        val long = marker.coordinate.longitude
        val data = "$lat,$long"
        val detailFragment = GoToRouteFragment.newInstance(data)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_content_main, detailFragment)
            .addToBackStack(null)
            .commit()
    }
    fun onItemClick(position: Int) {

        Toast.makeText(context, "Item clicked at position $position", Toast.LENGTH_SHORT).show()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        androidLocationProvider = null

    }
}