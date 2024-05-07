package com.example.flushhubproto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tomtom.databinding.QueryLoadingPageBinding

class QueryLoadingFragment: Fragment() {
    private val binding get() = _binding!!
    private var _binding: QueryLoadingPageBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = QueryLoadingPageBinding.inflate(inflater, container, false)

        return binding.root
    }

}