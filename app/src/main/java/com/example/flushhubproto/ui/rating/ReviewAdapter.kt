package com.example.flushhubproto.ui.rating
import android.util.Log
import android.view.LayoutInflater
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tomtom.R
import com.example.tomtom.databinding.FragmentRatingsBinding
import com.example.tomtom.databinding.ReviewListItemBinding

class ReviewAdapter(
    private var data: List<BathroomReviewData>,
    private val listener: ReviewInteractionListener
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {
    interface ReviewInteractionListener {
        fun onShowReviewsRequested(reviews: List<String>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ReviewListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val ratingBinding = FragmentRatingsBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ReviewViewHolder(binding,ratingBinding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        // Position + 1 since positions start at 0 but ranks start at 1
        holder.bind(data[position], position + 1,listener)
    }

    override fun getItemCount(): Int = data.size
    fun updateData(newData: List<BathroomReviewData>) {
        this.data = newData
        notifyDataSetChanged()

    }


    class ReviewViewHolder(
        private var binding: ReviewListItemBinding,
        private var ratingsBinding: FragmentRatingsBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bathroomData: BathroomReviewData, rank: Int, listener: ReviewInteractionListener) {
            binding.reviewListItemBuilding.text = bathroomData.buildingName
            if (bathroomData.gender == "All Gender") {
                binding.restroomListItemGender.setImageResource(R.drawable.gender_nuetral_icon)
            }
            binding.restroomListRankNum.text = "#$rank"
            binding.reviewListRateBar.rating = bathroomData.averageRating
            binding.reviewListRateBar.isClickable = false


            binding.showBathroomReviewsListButton.setOnClickListener {
                Log.d("Show bathroom review button","Button clicked....")
                ratingsBinding.showIndividualReviewList.showReview.visibility = VISIBLE
                listener.onShowReviewsRequested(bathroomData.reviews)
                val reviewsAdapter = IndividualReviewsListAdapter(ratingsBinding.root.context, bathroomData.reviews)
                ratingsBinding.showIndividualReviewList.individualReviewListView.adapter = reviewsAdapter


            }
        }
    }
}


