package com.example.flushhubproto

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flushhubproto.schema.test
import com.example.tomtom.R

class LocationInfoAdapter(private var locationList: List<test>) : RecyclerView.Adapter<LocationInfoAdapter.LocationViewHolder>() {
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newLocationList: List<test>) {
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


        fun bind(location: test) {
            addressTextView.text = location.Location
            distanceTextView.text = "0.1m"
            timeTextView.text = "10 min"
        }
    }
}

