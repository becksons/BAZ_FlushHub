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
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import java.io.File
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
        bathroomViewModel.selectedLocation.observe(viewLifecycleOwner) { details ->
            binding.detailsTextView.text = details
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        bathroomViewModel = ViewModelProvider(requireActivity())[BathroomViewModel::class.java]
        childFragmentManager.beginTransaction()
        .replace(R.id.map_container, MainActivity.mapFragment)
        .commit()

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
                    MainActivity.mapFragment.getMapAsync { tomtomMap ->
                        markMap(tomtomMap, latitude, longitude, address, distance, time, stars)
                    }
                }
            }
        }


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

    //This function adds markers to the TomTomMap API fragment
    private fun markMap(tomtomMap: TomTomMap, lat: Double, long: Double, address: String = "Bathroom", distance: String = "0", eta: String = "0", rating: String = "0.0") {
        val loc = GeoPoint(lat, long) //converting lat and long to GeoPoint type
        val markerOptions = MarkerOptions( //assigning informations of this marker
            coordinate = loc,
            pinImage = ImageFactory.fromResource(R.drawable.bathroom_location_icon),
            tag = "Address: ${address}\n" +
                    "Distance: $distance" + requireContext().getString(R.string.miles) +
                    "\nETA: $eta" + requireContext().getString(R.string.minutes) +
                    "\nRating: $rating" + requireContext().getString(R.string.stars)
        )

        //adding marker to the map
        tomtomMap.addMarker(markerOptions)

        //making the marker clickable
        tomtomMap.addMarkerClickListener { clickedMarker ->
            val detailText = clickedMarker.tag

            if (detailText != null) {
                bathroomViewModel.updateSelectedLocation(detailText)
            }


            Log.d("MarkerClick", "Marker at $address was clicked.")

            //show a UI if click
            showGoToRouteLayout(clickedMarker.coordinate.latitude, clickedMarker.coordinate.longitude, clickedMarker.tag!!, )

        }
    }

    //This function reveals a layout for the user to launch Google Maps to route them
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        androidLocationProvider = null
    }
}