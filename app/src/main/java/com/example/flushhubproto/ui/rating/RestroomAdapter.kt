package com.example.flushhubproto.ui.rating

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tomtom.R

class RestroomAdapter(private var restrooms: List<Restroom>, private val listener: OnItemClickListener) : RecyclerView.Adapter<RestroomAdapter.RestroomViewHolder>() {

    interface OnItemClickListener {

        fun navigateToSubmitReviewFragment(restroomId: Int)


        fun onReviewsClicked(restroomId: Int)
        fun onLeaveReviewClicked(restroomId: Int)
    }

    inner class RestroomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(restroom: Restroom, listener: OnItemClickListener) {


            itemView.findViewById<Button>(R.id.submit_button)?.setOnClickListener {
                listener.navigateToSubmitReviewFragment(restroom.id)
            }
            val rankNum = "#${restroom.id}"
            val topBuilding = "Building: ${restroom.building}"
            itemView.findViewById<TextView>(R.id.restroom_list_rank_num)?.text = rankNum
            itemView.findViewById<TextView>(R.id.review_list_item_building)?.text =
                restroom.building
            itemView.findViewById<TextView>(R.id.review_list_item_floor)?.text =
                restroom.floor.toString()
            itemView.findViewById<RatingBar>(R.id.review_list_rate_bar)?.apply {
                rating = restroom.rating.dec()
                isClickable = false
            }
            if(restroom.gender == 0){
                itemView.findViewById<ImageView>(R.id.restroom_list_item_gender)
                    ?.setImageResource(R.drawable.gender_nuetral_icon)

            }
            if(restroom.gender == 1){
                itemView.findViewById<ImageView>(R.id.restroom_list_item_gender)
                    ?.setImageResource(R.drawable.woman_icon)

            }
            if(restroom.gender == 2){
                itemView.findViewById<ImageView>(R.id.restroom_list_item_gender)
                    ?.setImageResource(R.drawable.man_icon)

            }










        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestroomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_list_item, parent, false)
        return RestroomViewHolder(view)
    }


    override fun onBindViewHolder(holder: RestroomViewHolder, position: Int) {
        holder.bind(restrooms[position], listener)
    }
    fun updateData(newData: List<Restroom>) {
        this.restrooms = newData
        notifyDataSetChanged()
    }



    override fun getItemCount() = restrooms.size
}
