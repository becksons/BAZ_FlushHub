package com.example.flushhubproto.ui.rating
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.flushhubproto.MainActivity
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

class RatingsFragment : Fragment() , ReviewAdapter.ReviewInteractionListener {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: ReviewAdapter
    private var refreshBool = false
    private var _binding: FragmentRatingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var bathroomViewModel: BathroomViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRatingsBinding.inflate(inflater, container, false)
        bathroomViewModel = ViewModelProvider(requireActivity())[BathroomViewModel::class.java]
        swipeRefreshLayout = binding.ratingsSwipeRefresh
        setupRecyclerView()


        //Getting bathroom data and creating instances of BathroomReviewData
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
                    binding.topRatingBar.isClickable = false

                    val topRatingNumString = roundToNearestHalf(it.averageRating).toString()
                    binding.topRatingNum.text = topRatingNumString
                }
            } else {
                Log.d("Ratings Fragment", "Review data null")
            }
        }
        return binding.root
    }
    private fun roundToNearestHalf(num: Float): Double {
        return (num * 2).roundToInt() / 2.0
    }

    //The review list layout is visible when a user clicks on a reviews button in a recycler view item
    override fun onShowReviewsRequested(reviews: BathroomReviewData) {

        val reviewsAdapter = IndividualReviewsListAdapter(requireContext(), reviews.reviews)
        binding.showIndividualReviewList.individualReviewListView.adapter = reviewsAdapter
        binding.showIndividualReviewList.root.visibility = View.VISIBLE
        binding.showIndividualReviewList.reviewListBuildingName.text = reviews.buildingName
        val avgRankString = reviews.averageRating.toString()
        binding.showIndividualReviewList.reviewListBuildingAvgRank.text = avgRankString
    }

    private fun setupRecyclerView() {
        adapter = ReviewAdapter(emptyList(),this)
        binding.ratingsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.ratingsRecyclerView.adapter = adapter
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.leaveReview.setOnClickListener {
            findNavController().navigate(R.id.submitReviewFragment)
        }
        binding.showIndividualReviewList.reviewListBackButton.setOnClickListener {
            binding.showIndividualReviewList.root.visibility = View.GONE
        }
        swipeRefreshLayout.setOnRefreshListener {
            MainActivity.swipeReviewLoading.postValue(true)
            refreshBool = true
            bathroomViewModel.loadAllBathrooms() // sets this to false after computation
            MainActivity.swipeReviewLoading.observe(viewLifecycleOwner) {
                swipeRefreshLayout.isRefreshing = it // Checks if its true or false
                if (!it && refreshBool) {
                    refreshBool = false
                    Toast.makeText(context, "Reviews Refreshed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
