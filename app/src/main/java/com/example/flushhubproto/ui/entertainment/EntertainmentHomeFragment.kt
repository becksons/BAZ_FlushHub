package com.example.flushhubproto.ui.entertainment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tomtom.R
import com.example.tomtom.databinding.FlushhubEntertainmentHomeFragmentBinding

class EntertainmentHomeFragment:Fragment() {
    private var _binding: FlushhubEntertainmentHomeFragmentBinding? = null
    val binding get()=  _binding!!
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FlushhubEntertainmentHomeFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.navEntertainmentAnimals.setOnClickListener {
            findNavController().navigate(R.id.randomAnimalFragment)
        }
        binding.navEntertainmentBoggle.setOnClickListener {
            findNavController().navigate(R.id.flushHubBoggle)

        }
        binding.navEntertainmentHangman.setOnClickListener {
            findNavController().navigate(R.id.newsListFragment)
        }

    }
}