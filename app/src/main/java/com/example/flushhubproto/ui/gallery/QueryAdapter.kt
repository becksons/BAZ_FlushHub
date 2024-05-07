package com.example.flushhubproto

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.flushhubproto.schema.bathroom
import com.example.flushhubproto.ui.home.BathroomViewModel
import com.example.tomtom.R
import com.tomtom.sdk.map.display.marker.Marker


class QueryAdapter(private var locationList: List<Marker>) : RecyclerView.Adapter<QueryAdapter.QueryViewHolder>() {
    private lateinit var bathroomViewModel: BathroomViewModel


    interface QueryAdapterListener {

        fun onItemClick(position: Int, item: Triple<bathroom, Double, Double>)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newLocationList:List<Marker>) {
        locationList = newLocationList

        Log.d("update data adapter", "Updating data in adapter...")
        notifyDataSetChanged()


    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_info_item, parent, false)
        return QueryViewHolder(view)
    }

    override fun onBindViewHolder(holder: QueryViewHolder, position: Int) {

        val location = locationList[position]
        holder.bind(location)
    }

    override fun getItemCount() = locationList.size


    inner class QueryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        fun bind(location: Marker) {
            val locationInfo= location.tag?.split("\n")
            val address = locationInfo?.get(0)
            val distance = locationInfo?.get(1)
            val time = locationInfo?.get(2)
            Log.d("Query recycler view: address: ", address.toString())

            //setting up the display for distance of the bathroom in the recycler view
            addressTextView.text = address
            if (distance?.toDouble()== -1.0) {
                distanceTextView.text = "N/A"
            } else {
                distanceTextView.text = metersToMiles(distance!!.toDouble()) + itemView.context.getString(R.string.miles)
            }

            if (time?.toDouble() == -1.0) {
                timeTextView.text = "N/A"
            } else {
                timeTextView.text = "$time" + itemView.context.getString(R.string.minutes)
            }
        }
    }
}

