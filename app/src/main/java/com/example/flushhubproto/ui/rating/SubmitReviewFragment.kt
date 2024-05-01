package com.example.flushhubproto.ui.rating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        binding.submitButton.setOnClickListener {
            findNavController().navigate(R.id.nav_slideshow)
        }
    }

}