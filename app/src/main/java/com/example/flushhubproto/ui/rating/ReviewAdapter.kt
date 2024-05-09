package com.example.flushhubproto.ui.rating
import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tomtom.databinding.FragmentRatingsBinding
import com.example.tomtom.databinding.ReviewListItemBinding

class ReviewAdapter( //Adapter for the recycler list view in the ratings fragment
    private var data: List<BathroomReviewData>,
    private val listener: ReviewInteractionListener
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {
    interface ReviewInteractionListener {//defining listener for the review button
        fun onShowReviewsRequested(reviews: BathroomReviewData)
    }

    //Using 2 bindings for both the review list items and rating fragment to access the individual bathroom review list layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ReviewListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val ratingBinding = FragmentRatingsBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ReviewViewHolder(binding,ratingBinding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(data[position], position + 1,listener)
    }

    override fun getItemCount(): Int = data.size
    @SuppressLint("NotifyDataSetChanged")
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
//            if (bathroomData.gender == "All Gender") {
//                binding.restroomListItemGender.setImageResource(R.drawable.gender_nuetral_icon)
//            }
            binding.restroomListRankNum.text = "#$rank"
            binding.reviewListRateBar.rating = bathroomData.averageRating
            binding.reviewListRateBar.isClickable = false

            //When individual bathroom review button is selected, onShowReviewsRequested is called in ratings fragment
            binding.showBathroomReviewsListButton.setOnClickListener {
                Log.d("Show bathroom review button","Button clicked....")
                ratingsBinding.showIndividualReviewList.showReview.visibility = VISIBLE
                ratingsBinding.showIndividualReviewList.reviewListBuildingName.text = bathroomData.buildingName
                listener.onShowReviewsRequested(bathroomData)
                val reviewsAdapter = IndividualReviewsListAdapter(ratingsBinding.root.context, bathroomData.reviews)
                ratingsBinding.showIndividualReviewList.individualReviewListView.adapter = reviewsAdapter



            }
        }
    }
}


