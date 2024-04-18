package com.example.flushhubproto.ui.rating

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tomtom.R

class RestroomAdapter(private val restrooms: List<Restroom>, private val listener: OnItemClickListener) : RecyclerView.Adapter<RestroomAdapter.RestroomViewHolder>() {


    interface OnItemClickListener {
        fun onReviewsClicked(restroomId: Int)
        fun onLeaveReviewClicked(restroomId: Int)
    }

    class RestroomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var restroomIDCount = 0
        fun bind(restroom: Restroom, listener: OnItemClickListener) {

            itemView.findViewById<TextView>(R.id.reviewNickname).text = restroom.name
//            restroom.reviews[restroom.id].plus(itemView.findViewById<TextView>(R.id.review).text)
//            itemView.findViewById<Button>(R.id.submitReviewButton).setOnClickListener {
//                listener.onReviewsClicked(restroom.id)
//            }
            itemView.findViewById<RatingBar>(R.id.reviewRatingBar).setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                restroom.rating = rating
            }
            restroom.id =restroomIDCount
            restroomIDCount++
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestroomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_submit_review, parent, false)
        return RestroomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestroomViewHolder, position: Int) {
        holder.bind(restrooms[position], listener)
    }

    override fun getItemCount() = restrooms.size
}
