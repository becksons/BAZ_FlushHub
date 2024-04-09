
package com.example.flushhubproto.ui.home

import android.Manifest
import android.content.pm.PackageManager
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
import com.example.flushhubproto.LocationInfoAdapter
import com.example.tomtom.R
import com.example.tomtom.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.UiComponentClickListener
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient




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
                    // Handle deny permission.
                }
            }
        }
    }

    private val mapOptions = MapOptions(mapKey ="AOYMhs1HWBhlfnU4mIaiSULFfvNGTw4Z")
    private val mapFragment = MapFragment.newInstance(mapOptions)

    private fun initializeMapWithLocation() {
        childFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()


        viewLifecycleOwner.lifecycleScope.launch {
            moveMap(42.34997406716152,-71.1032172645369 )
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
                pinImage = ImageFactory.fromResource(R.drawable.star_icon)
            )

            tomtomMap.addMarker(markerOptions)
            tomtomMap.moveCamera(cameraOptions)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
