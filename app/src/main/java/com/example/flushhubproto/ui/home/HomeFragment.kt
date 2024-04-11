
package com.example.flushhubproto.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.tomtom.R
import com.example.tomtom.databinding.FragmentHomeBinding
import com.tomtom.quantity.Distance
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.android.AndroidLocationProvider
import com.tomtom.sdk.location.android.AndroidLocationProviderConfig
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.ui.MapFragment
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

        mapFragment.getMapAsync {tomtomMap ->
            tomtomMap.setLocationProvider(androidLocationProvider)
        }
        androidLocationProvider?.enable()

        viewLifecycleOwner.lifecycleScope.launch {
            androidLocationProvider?.lastKnownLocation?.position?.let { moveMap(it.latitude,it.longitude ) }
        }
    }

    fun moveMap(lat: Double, long: Double){
        mapFragment.getMapAsync { tomtomMap ->
            val cameraOptions = CameraOptions(
                position = GeoPoint(lat, long),
                zoom = 17.0,
                tilt = 0.0,
                rotation = 0.0
            )

            val cds = GeoPoint(lat, long)
            val markerOptions = MarkerOptions(
                coordinate = cds,
                pinImage = ImageFactory.fromResource(R.drawable.bathroom_location_icon)
            )

            tomtomMap.addMarker(markerOptions)
            tomtomMap.moveCamera(cameraOptions)
        }
    }

    //distance and timeaway for display given coords




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        androidLocationProvider = null
    }
}
