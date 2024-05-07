package com.example.flushhubproto.ui.rating
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flushhubproto.ui.home.BathroomViewModel
import com.example.tomtom.R
import com.example.tomtom.databinding.FragmentRatingsBinding

class RatingsFragment : Fragment() {

    private var _binding: FragmentRatingsBinding? = null
    private lateinit var adapter: ReviewAdapter
    private val binding get() = _binding!!
    private val viewModel: BathroomViewModel by viewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRatingsBinding.inflate(inflater, container, false)


        setupRecyclerView()
        viewModel.bathrooms.observe(viewLifecycleOwner) { bathrooms ->
            Log.d("Ratings Fragment", "Getting review data...")
            if (bathrooms != null) {
                val reviewMap = bathrooms.associate {
                    it.first._id.toString() to it.first.Reviews.split("=")
                }
                adapter.updateData(reviewMap)
            }else{
                Log.d("Ratings Fragment", "Review data null")
            }
        }

//        viewModel.reviewList.observe(viewLifecycleOwner) { reviewMap ->
//            if (reviewMap != null) {
//                adapter.updateData(reviewMap)
//            }
//        }


        return binding.root
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

    companion object {
        fun newInstance() = RatingsFragment()
    }
}
