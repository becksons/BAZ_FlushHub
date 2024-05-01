package com.example.flushhubproto.ui.rating

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tomtom.R
import com.example.tomtom.databinding.FragmentRatingsBinding

class RatingsFragment : Fragment() {

    private var _binding: FragmentRatingsBinding? = null
    private lateinit var viewModel: DummyReviewsViewModel
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRatingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ratingsRecyclerView.layoutManager = LinearLayoutManager(context)


        val fakeData = listOf(
            Restroom(id = 0, name = "Restroom A",floor =  1, building = "CDS",gender = 0, rating =  3.5f, reviews = emptyList()),
            Restroom(id = 1, name ="Restroom B", floor =2, building = "MSC", rating= 4.5f ,gender = 2,reviews = emptyList()),
            Restroom(id = 2 , name ="Restroom C", floor =3, building = "CAS", rating= 2.0f,gender = 1, reviews = emptyList())
        )
        binding.ratingsRecyclerView.adapter = RestroomAdapter(fakeData, object : RestroomAdapter.OnItemClickListener {
            override fun navigateToSubmitReviewFragment(restroomId: Int) {
                val bundle = Bundle()
                bundle.putInt("restroom_id", restroomId)
                val submitReviewFragment = SubmitReviewFragment()
                submitReviewFragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, submitReviewFragment)
                    .addToBackStack(null)
                    .commit()
            }

            override fun onReviewsClicked(restroomId: Int) {
                //review click here
            }
            override fun onLeaveReviewClicked(restroomId: Int) {
                //leave review click here
            }
        })

        viewModel = ViewModelProvider(this)[DummyReviewsViewModel::class.java]
        viewModel.ratings.observe(viewLifecycleOwner) { ratings ->
            (binding.ratingsRecyclerView.adapter as RestroomAdapter).updateData(ratings)
            binding.emptyView.visibility = if (ratings.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.leaveReview.setOnClickListener {
            findNavController().navigate(R.id.submitReviewFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
