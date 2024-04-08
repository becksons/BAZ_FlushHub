package com.example.flushhubproto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tomtom.R

class LocationInfoAdapter(private val items: List<LocationInfo>) : RecyclerView.Adapter<LocationInfoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val addressTextView: TextView = view.findViewById(R.id.address_text_view)
        val distanceTextView: TextView = view.findViewById(R.id.distance_text_view)
        val timeTextView: TextView = view.findViewById(R.id.time_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_info_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.addressTextView.text = item.address
        holder.distanceTextView.text = item.distance
        holder.timeTextView.text = item.time
    }

    override fun getItemCount() = items.size
}
