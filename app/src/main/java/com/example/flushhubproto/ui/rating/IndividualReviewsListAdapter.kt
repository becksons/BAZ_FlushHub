package com.example.flushhubproto.ui.rating

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RatingBar
import android.widget.TextView
import com.example.tomtom.R
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.round

//List adapter that is used for individual review lists
class IndividualReviewsListAdapter(context: Context, reviews: List<String>) :
    ArrayAdapter<String>(context, R.layout.list_of_reviews_item, reviews) {
    val numericRatings = reviews.map {//Parsing review strings to get the rating numbers
        try {
            it.substringBefore('$').toDouble()
        } catch (e: NumberFormatException) {
            0.0
        }
    }
    private fun roundToNearestHalf(num: Float): Float {
        return round(num * 2) / 2
    } //Rounding rating number
    fun roundOffDecimal(number: Double): Double? {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.FLOOR
        return df.format(number).toDouble()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_of_reviews_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.individual_review_text)
        val reviewRating  = view.findViewById<RatingBar>(R.id.individual_review_rating_bar)
        val reviewRatingNum  = view.findViewById<TextView>(R.id.individual_review_num_stars)

        textView.text = getItem(position)?.substringAfter('$', "Missing review")
        val roundedRating= numericRatings[position]
        reviewRating.rating = roundedRating.toFloat() // set the rounded rating directly
        reviewRatingNum.text = String.format("%.1f stars", roundedRating) // showing the rating number as text
        reviewRating.numStars = 5
        return view
    }
}

