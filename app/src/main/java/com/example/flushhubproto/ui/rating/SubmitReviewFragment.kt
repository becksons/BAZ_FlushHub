package com.example.flushhubproto.ui.rating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.flushhubproto.ui.home.BathroomViewModel
import com.example.tomtom.R
import com.example.tomtom.databinding.FragmentSubmitReviewBinding

class SubmitReviewFragment: Fragment() {
    private var _binding: FragmentSubmitReviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var bathroomViewModel: BathroomViewModel
    companion object {
        private const val ARG_RESTROOM_ID = "restroom_id"
        fun newInstance(restroomId: Int) =
            SubmitReviewFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_RESTROOM_ID, restroomId)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSubmitReviewBinding.inflate(inflater, container, false)
        bathroomViewModel = ViewModelProvider(requireActivity())[BathroomViewModel::class.java]
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buildingData = mutableListOf<String>()


        bathroomViewModel.bathrooms.observe(viewLifecycleOwner) { bathrooms->
            bathrooms?.forEach {  bathroom->
                buildingData.add(
                    bathroom.first.Name
                )
            }
        }

        binding.buildingSpinner.adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, buildingData)
        binding.submitButton.setOnClickListener {
            findNavController().navigate(R.id.nav_slideshow)
        }
        binding.buildingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                val selectedBuilding = parent.getItemAtPosition(position).toString()
                // Do something with the selected building
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle no selection
            }

        }
        binding.submitReviewBackButton.setOnClickListener {
            findNavController().navigate(R.id.ratingsFragment)
        }
        val floorData = listOf("1", "2", "3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20")

        binding.floorSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, floorData)

        binding.floorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                val selectedBuilding = parent.getItemAtPosition(position).toString()
                // Do something with the selected building
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle no selection
            }
        }

}}