package com.example.flushhubproto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tomtom.R

class LocationInfoAdapter(private var locationList: List<LocationInfo>) : RecyclerView.Adapter<LocationInfoAdapter.LocationViewHolder>() {
    fun updateData(newLocationList: List<LocationInfo>) {
        locationList = newLocationList
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


    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val addressTextView: TextView = itemView.findViewById(R.id.address_text_view)
        private val distanceTextView: TextView = itemView.findViewById(R.id.distance_text_view)
        private val timeTextView: TextView = itemView.findViewById(R.id.time_text_view)


        fun bind(location: LocationInfo) {
            addressTextView.text = location.address
            distanceTextView.text = location.distance
            timeTextView.text = location.time
        }
    }
}

