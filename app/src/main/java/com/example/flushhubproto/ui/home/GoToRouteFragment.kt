package com.example.flushhubproto.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tomtom.R

class GoToRouteFragment : Fragment() {

    companion object {
        private const val ARG_COORDINATES = "coordinates"

        fun newInstance(coordinates: String): GoToRouteFragment {
            val fragment = GoToRouteFragment()
            val args = Bundle()
            args.putString(ARG_COORDINATES, coordinates)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var mapButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_go_to_route, container, false)
        val coordinates = arguments?.getString(ARG_COORDINATES) ?: "0,0"
        val (lat, lon) = coordinates.split(",").map { it.toDouble() }

        setupViews(view, lat, lon)
        return view
    }

    private fun setupViews(view: View, lat: Double, lon: Double) {
        val textView = view.findViewById<TextView>(R.id.details_text_view)
        textView.text = "Coordinates: $lat, $lon"

        mapButton = view.findViewById(R.id.map_button)
        mapButton.setOnClickListener {
            openMap(lat, lon, "Destination")

        }
    }

    private fun openMap(lat: Double, lon: Double, label: String) {
        val geoUri = Uri.parse("geo:0,0?q=$lat,$lon($label)")
        val intent = Intent(Intent.ACTION_VIEW, geoUri)
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }
}
