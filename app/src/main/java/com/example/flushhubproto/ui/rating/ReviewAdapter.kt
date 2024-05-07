package com.example.flushhubproto.ui.rating
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flushhubproto.MainActivity
import com.example.tomtom.databinding.ReviewListItemBinding
class ReviewAdapter(
    private var data: List<Map.Entry<String, List<String>>>

) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ReviewListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: Map<String, List<String>>) {
        this.data = newData.entries.toList()
        notifyDataSetChanged()
    }

    class ReviewViewHolder(
        private var binding: ReviewListItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(entry: Map.Entry<String, List<String>>) {
            binding.restroomListRankNum.text = "Bathroom ID: ${entry.key}"
            binding.showBathroomReviewsListButton.setOnClickListener {

                //MainActivity.reviewButtonClicked.postValue(true)
            }
        }
    }
}
