package com.example.flushhubproto.ui.rating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tomtom.R
import com.example.tomtom.databinding.FragmentSubmitReviewBinding

class SubmitReviewFragment: Fragment() {
    private var _binding: FragmentSubmitReviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DummyReviewsViewModel
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
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val buildingData = listOf("Item 1", "Item 2", "Item 3")//

        binding.buildingSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, buildingData)
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
        val floorData = listOf("1", "2", "3")

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