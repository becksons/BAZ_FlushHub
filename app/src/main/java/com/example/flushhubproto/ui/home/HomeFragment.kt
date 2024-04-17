package com.example.flushhubproto.ui.home


import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView

import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
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

    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1

        private val bottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) { /* Handle state change */ }
                override fun onSlide(bottomSheet: View, slideOffset: Float) { /* Handle sliding */ }
        }

    }


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var androidLocationProvider: LocationProvider? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LocationInfoAdapter
    private lateinit var viewModel: HomeViewModel
    private var isExpanded = false


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

        context?.let { ctx ->
            openMap(ctx,42.350026020986256, -71.10326632227299) //pasing to Google Maps
        }
//
//        setupAppBarInteraction()


        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        setupRecyclerView(binding)
        observeLocationInfos()

        return binding.root
    }

//    private fun setupAppBarInteraction() {
//        val appBar = binding.nearestLocationRecyclerView.nearestRestAppbar // Make sure you have the correct reference to your AppBar
//        appBar.setOnClickListener {
//            if (isExpanded) {
//                collapseRecyclerView()
//            } else {
//                expandRecyclerView()
//            }
//            isExpanded = !isExpanded
//        }
//    }


    private fun setupRecyclerView(binding: FragmentHomeBinding) {
        recyclerView = binding.nearestLocationRecyclerView.nearestLocationRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = LocationInfoAdapter(emptyList())
        recyclerView.adapter = adapter
    }


    private fun observeLocationInfos() {
        viewModel.locationInfos.observe(viewLifecycleOwner) { locationInfos ->
            adapter.updateData(locationInfos)
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

                // Move map to the new location
                moveMap(tomtomMap, location.position.latitude, location.position.longitude)
                updateUserLocationOnMap(tomtomMap,location.position.latitude,location.position.longitude)
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
    fun openMap(context: Context, lat: Double, long: Double, label: String = "Mark") {
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

    fun markMap(tomtomMap: TomTomMap, lat: Double, long: Double){
        val loc = GeoPoint(lat, long)
        val markerOptions = MarkerOptions(
            coordinate = loc,
            pinImage = ImageFactory.fromResource(R.drawable.bathroom_location_icon)
        )

        tomtomMap.addMarker(markerOptions)
    }

    //distance and timeaway for display given coords




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        androidLocationProvider = null

    }
}