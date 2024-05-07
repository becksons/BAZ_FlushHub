package com.example.flushhubproto.ui.rating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flushhubproto.ui.home.BathroomViewModel
import com.example.tomtom.databinding.ListOfIndividualReviewsBinding
class ReviewDetailsFragment : Fragment() {
    private var _binding: ListOfIndividualReviewsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BathroomViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ListOfIndividualReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val bathroomId = arguments?.getString("BATHROOM_ID") ?: ""
        binding.individualReviewRecyclerList.layoutManager = LinearLayoutManager(context)

        viewModel.reviewList.observe(viewLifecycleOwner) { reviewsMap ->
            val reviews = reviewsMap[bathroomId] ?: listOf()
            val adapter = ReviewListAdapter(reviews)
            binding.individualReviewRecyclerList.adapter = adapter
        }
    }

    companion object {
        fun newInstance(bathroomId: String) = ReviewDetailsFragment().apply {
            arguments = Bundle().apply {
                putString("BATHROOM_ID", bathroomId)
            }
        }
    }
}
