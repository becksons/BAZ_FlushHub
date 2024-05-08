package com.example.flushhubproto.ui.rating
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flushhubproto.ui.home.BathroomViewModel
import com.example.tomtom.R
import com.example.tomtom.databinding.FragmentRatingsBinding
import kotlin.math.roundToInt

data class BathroomReviewData(
    val bathroomId: String,
    val buildingName: String,
    val gender: String,
    val reviews: List<String>,
    val averageRating: Float
)

class RatingsFragment : Fragment() {
    private lateinit var adapter: ReviewAdapter
    private var _binding: FragmentRatingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var bathroomViewModel: BathroomViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRatingsBinding.inflate(inflater, container, false)
        bathroomViewModel = ViewModelProvider(requireActivity())[BathroomViewModel::class.java]
        setupRecyclerView()


        bathroomViewModel.bathrooms.observe(viewLifecycleOwner) { bathrooms ->
            Log.d("Ratings Fragment", "Getting review data...")
            if (bathrooms != null) {
                val reviewDataList = bathrooms.map {
                    val reviews = it.first.Reviews.split("=")
                    val ratings = reviews.mapNotNull { review ->
                        review.split("$").firstOrNull()?.toFloatOrNull()
                    }
                    val averageRating = if (ratings.isNotEmpty()) ratings.average().toFloat() else 0F

                    BathroomReviewData(
                        bathroomId = it.first._id.toString(),
                        buildingName = it.first.Name,
                        gender = it.first.Type,
                        reviews = reviews,
                        averageRating = averageRating
                    )
                }.sortedByDescending { it.averageRating }

                adapter.updateData(reviewDataList)


                reviewDataList.firstOrNull()?.let {
                    binding.topBuilding.text = it.buildingName
                    binding.topRatingBar.rating= it.averageRating
                    binding.topRatingNum.text = roundToNearestHalf(it.averageRating).toString() + " stars"

                }
            } else {
                Log.d("Ratings Fragment", "Review data null")
            }
        }


        return binding.root
    }
    fun roundToNearestHalf(num: Float): Double {
        return (num * 2).roundToInt() / 2.0
    }

    private fun setupRecyclerView() {
        adapter = ReviewAdapter(emptyList())
        binding.ratingsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.ratingsRecyclerView.adapter = adapter
    }

    private fun navigateToReviewDetails(bathroomId: String) {
        val fragment = ReviewDetailsFragment.newInstance(bathroomId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_content_main, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.leaveReview.setOnClickListener {
            findNavController().navigate(R.id.submitReviewFragment)
        }
    }

    companion object {
        fun newInstance() = RatingsFragment()
    }
}
