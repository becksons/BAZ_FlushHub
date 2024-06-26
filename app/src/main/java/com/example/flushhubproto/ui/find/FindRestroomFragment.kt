package com.example.flushhubproto.ui.find

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flushhubproto.MainActivity
import com.example.flushhubproto.ui.home.BathroomViewModel
import com.example.tomtom.R
import com.example.tomtom.databinding.FragmentFindBinding


class FindRestroomFragment : Fragment() {

    private var _binding: FragmentFindBinding? = null
    private lateinit var bathroomViewModel: BathroomViewModel
    private val binding get() = _binding!!
    private var currentQuery: MutableList<String> = mutableListOf("All Gender", "all", "3.0") //default values

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindBinding.inflate(inflater, container, false)
        val root: View = binding.root

        bathroomViewModel = ViewModelProvider(requireActivity())[BathroomViewModel::class.java]
        binding.genderRadioGroup.setOnCheckedChangeListener { group, checkedId -> //check user input for gender
            val gender = when (checkedId) {
                R.id.gender_male -> "Male"
                R.id.gender_female -> "Female"
                R.id.gender_neutral ->"All Gender"
                else -> "All Gender"
            }
            handleGenderSelection(gender)
        }

        bathroomViewModel.queriedBathrooms.observe(viewLifecycleOwner) { dataList -> //getting the bathrooms data
            dataList?.forEach { data->
                val parts = data.first.Coordinates.split(',')
                val longitude: Double = parts[0].toDouble()
                val latitude: Double = parts[1].toDouble()
                val address: String = data.first.Location
                val stars: Double = data.first.Rating
                Log.d("QUERY INFO","[FLUSHUB QUERY] Found Bathroom at $longitude, $latitude at $address with $stars stars.")
            }
        }

        setupListeners()
        return root
    }
    private fun handleGenderSelection(gender: String) {
        currentQuery[0] = gender
    }

    private fun setupListeners() {
        //handling user input for campus area
        binding.campusEast.setOnClickListener {
            handleCampusSelection("east")
            updateButtonState(it)
        }
        binding.campusCentral.setOnClickListener {
            handleCampusSelection("central")
            updateButtonState(it)
        }
        binding.campusWest.setOnClickListener {
            handleCampusSelection("west")
            updateButtonState(it)
        }

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            handleRatingChange(rating)
        }

        binding.findButton.setOnClickListener {
            Log.d("Find Button","Find Button Clicked!")
            Log.d("Find Button","Current Query: $currentQuery")

            MainActivity.isQueryLoading.postValue(true)

            bathroomViewModel.queryBathroomsFullQuery(
                currentQuery[0],
                currentQuery[1],
                currentQuery[2].toDouble(),
                MainActivity.currentLatitude,
                MainActivity.currentLongitude
            )
            bathroomViewModel.searchQuery.postValue(Triple(currentQuery[0],currentQuery[1],currentQuery[2]))
        }
    }

    private fun handleCampusSelection(campus: String) {
        currentQuery[1] = campus
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    //Updating button UI's when one is clicked
    private fun updateButtonState(selectedButton: View) {
        listOf(binding.campusEast, binding.campusCentral, binding.campusWest).forEach {
            it.isEnabled = it != selectedButton
        }
    }

    private fun handleRatingChange(rating: Float) {
        currentQuery[2] = rating.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
