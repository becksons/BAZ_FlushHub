package com.example.flushhubproto.ui.gallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tomtom.R
import com.example.tomtom.databinding.FragmentFindBinding


class FindRestroomFragment : Fragment() {
    private var _binding: FragmentFindBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.genderRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val gender = when (checkedId) {
                R.id.gender_male -> "Male"
                R.id.gender_female -> "Female"
                R.id.gender_neutral ->"Gender Neutral"
                else -> ""
            }
            handleGenderSelection(gender)
        }

        setupListeners()
        return root
    }
    private fun handleGenderSelection(gender: String) {
        // Conditional logic based on gender
        if (gender == "Male") {
            // Do something for Male

        } else if (gender == "Female") {
            // Do something for Female
        } else if (gender == "Gender Neutral") {
        // Do something for Gender nuetral
        }else{
            //Handle error
        }
        Log.d("Gender","Gender: $gender ")

    }

    private fun setupListeners() {
        binding.campusEast.setOnClickListener {
            handleCampusSelection("East")
            updateButtonState(it)
        }
        binding.campusCentral.setOnClickListener {
            handleCampusSelection("Central")
            updateButtonState(it)
        }
        binding.campusWest.setOnClickListener {
            handleCampusSelection("West")
            updateButtonState(it)
        }

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            handleRatingChange(rating)
        }
    }

    private fun handleCampusSelection(campus: String) {
        println("Selected campus: $campus")
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

    private fun handleRatingChange(rating: Float) {
        println("Rating: $rating")
        if (rating >= 4.0) {

            println("High rating selected")


        } else if (rating < 4.0 && rating >= 2.0) {
            println("Mid rating selected")
        } else {
            println("Low rating selected")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
