package com.example.flushhubproto.ui.rating
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tomtom.R
import com.example.tomtom.databinding.ReviewListItemBinding

class ReviewAdapter(
    private var data: List<BathroomReviewData>
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ReviewListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        // Position + 1 since positions start at 0 but ranks start at 1
        holder.bind(data[position], position + 1)
    }

    override fun getItemCount(): Int = data.size
    fun updateData(newData: List<BathroomReviewData>) {
        this.data = newData
        notifyDataSetChanged()

    }


    class ReviewViewHolder(
        private var binding: ReviewListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bathroomData: BathroomReviewData, rank: Int) {
            binding.reviewListItemBuilding.text = bathroomData.buildingName
            if(bathroomData.gender=="All Gender"){
                binding.restroomListItemGender.setImageResource(R.drawable.gender_nuetral_icon)
            }
            binding.restroomListRankNum.text = "#$rank"

            binding.reviewListRateBar.rating = bathroomData.averageRating

        }
    }
}


