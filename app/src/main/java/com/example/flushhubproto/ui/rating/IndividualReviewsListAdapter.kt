package com.example.flushhubproto.ui.rating

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RatingBar
import android.widget.TextView
import com.example.tomtom.R


class IndividualReviewsListAdapter(context: Context, reviews: List<String>) :
    ArrayAdapter<String>(context, R.layout.list_of_reviews_item, reviews) {
    val ratings = reviews.map { it.substringBefore('$', "0") } // Default to "0" if no '$' is found
    val numericRatings = reviews.map {
        try {
            it.substringBefore('$').toInt()
        } catch (e: NumberFormatException) {
            0 // Default to 0 if parsing fails
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_of_reviews_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.individual_review_text)
        val reviewRating  = view.findViewById<RatingBar>(R.id.individual_review_rating_bar)
        val reviewRatingNum  = view.findViewById<TextView>(R.id.individual_review_num_stars)

        textView.text = getItem(position)?.substringAfter('$', "Missing review") // Default text if no '$' found
        reviewRating.numStars = 5
        reviewRating.rating = numericRatings[position].toFloat()
        reviewRatingNum.text = numericRatings[position].toString() + " Stars"



        return view
    }
}

