package com.example.flushhubproto

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flushhubproto.ui.home.BathroomViewModel
import com.example.tomtom.databinding.InitLoadingPageBinding

class InitLoadingFragment: Fragment() {
    private val binding get() = _binding!!
    private var _binding: InitLoadingPageBinding? = null
    private lateinit var bathroomViewModel: BathroomViewModel
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = InitLoadingPageBinding.inflate(inflater, container, false)
        bathroomViewModel = ViewModelProvider(requireActivity())[BathroomViewModel::class.java]
        MainActivity.loadStart = true

        MainActivity.isRealmInit.observe(viewLifecycleOwner) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                bathroomViewModel.loadAllBathrooms()
            } else if (MainActivity.currentLatitude == 42.3505 && MainActivity.currentLongitude == -71.1054){
                bathroomViewModel.loadAllBathrooms()
            }
        }

        return binding.root
    }
}