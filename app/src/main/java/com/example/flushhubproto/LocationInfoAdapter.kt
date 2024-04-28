package com.example.flushhubproto

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.flushhubproto.schema.test
import com.example.tomtom.R



class LocationInfoAdapter(private var locationList: List<Triple<test, Double, Double>>) : RecyclerView.Adapter<LocationInfoAdapter.LocationViewHolder>() {
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newLocationList: List<Triple<test, Double, Double>>?) {
        if (newLocationList != null) {
            locationList = newLocationList
        }
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_info_item, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locationList[position]
        holder.bind(location)
    }

    override fun getItemCount() = locationList.size


   inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addressTextView: TextView = itemView.findViewById(R.id.address_text_view)
        private val distanceTextView: TextView = itemView.findViewById(R.id.distance_text_view)
        private val timeTextView: TextView = itemView.findViewById(R.id.time_text_view)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                   //TODO: Figure out item listener for restroom recycler list
                    Toast.makeText(itemView.context,"Item clicked at $position",Toast.LENGTH_SHORT).show()
                }
            }
        }

       // Small Meter -> Miles Conversion Func.
       private fun metersToMiles(meters: Double): String {
           val conversionFactor = 0.000621371
           return String.format("%.1f", meters * conversionFactor)
       }

        @SuppressLint("SetTextI18n")
        fun bind(location: Triple<test, Double, Double>) {

            addressTextView.text = location.first.Location
            if (location.second == -1.0) {
                distanceTextView.text = "N/A"
            } else {
                distanceTextView.text = "${metersToMiles(location.second)} mi"
            }

            if (location.third == -1.0) {
                timeTextView.text = "N/A"
            } else {
                timeTextView.text = "${location.third} min"
            }
        }
    }
}

