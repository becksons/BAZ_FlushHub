package com.example.flushhubproto.ui.rating

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tomtom.R

class ReviewListAdapter(private var reviews: List<String>) :
    RecyclerView.Adapter<ReviewListAdapter.ReviewListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewListAdapter.ReviewListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_of_reviews_item, parent, false)
        return ReviewListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewListViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size

    class ReviewListViewHolder(private var view: View) :
        RecyclerView.ViewHolder(view) {

        fun bind(review: String) {

            view.findViewById<TextView>(R.id.individual_review_text).text = review// Assume `reviewContent` is your TextView ID
        }
    }
}

