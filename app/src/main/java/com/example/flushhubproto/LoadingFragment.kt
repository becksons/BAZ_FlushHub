package com.example.flushhubproto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tomtom.databinding.FragmentRatingsBinding
import com.example.tomtom.databinding.LoadingPageBinding

class LoadingFragment: Fragment() {
    private val binding get() = _binding!!
    private var _binding: LoadingPageBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LoadingPageBinding.inflate(inflater, container, false)
        return binding.root
    }
}