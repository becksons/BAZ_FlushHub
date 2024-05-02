package com.example.flushhubproto.ui.gallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.flushhubproto.ui.home.BathroomViewModel
import com.example.tomtom.R
import com.example.tomtom.databinding.FragmentFindBinding


class FindRestroomFragment : Fragment() {

    private var _binding: FragmentFindBinding? = null
    private lateinit var bathroomViewModel: BathroomViewModel
    private val binding get() = _binding!!
    private var currentQuery: MutableList<String> = mutableListOf("All Gender", "Central", "3.0")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFindBinding.inflate(inflater, container, false)
        val root: View = binding.root
        bathroomViewModel = ViewModelProvider(requireActivity())[BathroomViewModel::class.java]
        binding.genderRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val gender = when (checkedId) {
                R.id.gender_male -> "Male"
                R.id.gender_female -> "Female"
                R.id.gender_neutral ->"All Gender"
                else -> "All Gender"
            }
            handleGenderSelection(gender)

        }

        setupListeners()
        return root
    }
    private fun handleGenderSelection(gender: String) {
        currentQuery[0] = gender

    }

    private fun setupListeners() {
        var area: String = "all"
        binding.campusEast.setOnClickListener {
            handleCampusSelection("East")
            area = "east"
            updateButtonState(it)
        }
        binding.campusCentral.setOnClickListener {
            handleCampusSelection("Central")
            area = "central"
            updateButtonState(it)
        }
        binding.campusWest.setOnClickListener {
            handleCampusSelection("West")
            area = "west"
            updateButtonState(it)
        }

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            handleRatingChange(rating)
        }

        binding.findButton.setOnClickListener {
            Log.d("Find Button","Find Button Clicked!")
            Log.d("Find Button","Current Query: $currentQuery")
            findNavController().navigate(R.id.queryResultFragment)
            // Run Query
            // Switch to Loading Screen
            // Switch Back to Home
            bathroomViewModel.queryReady.postValue(true)
            bathroomViewModel.searchQuery.postValue(Triple(currentQuery[0],currentQuery[1],currentQuery[2]))
            bathroomViewModel.queryBathroomsFullQuery()
            bathroomViewModel.filterCriteria.postValue(area)
            Log.d("Find Button","View model filter criteria posted...")
//            homeFragment.filterMap(area)

        }
    }

    private fun handleCampusSelection(campus: String) {
        currentQuery[1] = campus

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    private fun updateButtonState(selectedButton: View) {
        listOf(binding.campusEast, binding.campusCentral, binding.campusWest).forEach {
            if (it == selectedButton) {
                it.isEnabled = false
            } else {
                it.isEnabled = true
            }
        }
    }
    fun applyFilter(criteria: String) {
        bathroomViewModel.setFilterCriteria(criteria)
    }

    private fun handleRatingChange(rating: Float) {
        currentQuery[2] = rating.toString()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
