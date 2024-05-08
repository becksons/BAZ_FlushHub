package com.example.flushhubproto.ui.bathroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.flushhubproto.MainActivity
import com.example.tomtom.R
import com.example.tomtom.databinding.FragmentBathroomBinding

class BathroomPage: Fragment() {
    private val binding get() = _binding!!
    private var _binding: FragmentBathroomBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreate(savedInstanceState)
        _binding = FragmentBathroomBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton: ImageButton = view.findViewById(R.id.bathroom_details_back_button)
        val bathroomName: TextView = view.findViewById(R.id.Bathroom_Name)
        val bathroomDescription: TextView = view.findViewById(R.id.Bathroom_Description)
        val bestReview: TextView = view.findViewById(R.id.Best_Review)
        val ratings: RatingBar = view.findViewById(R.id.ratingBathroomBar)
        val routeButton: Button = view.findViewById(R.id.Route_Button)


        bathroomName.text = MainActivity.currentBathroom?.first?.Name
        bathroomDescription.text = MainActivity.currentBathroom?.first?.Description

        if (MainActivity.currentBathroom != null) {
            ratings.rating = MainActivity.currentBathroom!!.first.Rating.toFloat()
        } else {
            ratings.rating = 0.0F
        }

        val rawReviewList = MainActivity.currentBathroom?.first?.Reviews?.split("=")
        if (rawReviewList != null) {
            val ratingReviewMasterList: MutableList<List<String>> = mutableListOf()
//            rawReviewList.forEach {rev ->
//                val ratingAndReview = rev.split("$")
//                ratingReviewMasterList.add(listOf(ratingAndReview[0], ratingAndReview[1])) // Rating | Review
//            }
            bestReview.text = "AKSJDHAS"
        }
    }

}