package com.example.flushhubproto

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.flushhubproto.schema.test
import com.example.flushhubproto.ui.home.HomeFragment
import com.example.tomtom.R
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.math.roundToInt


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

        // Calculates Distance and Travel time based on given Coordinates
        private fun calcRange(startLat: Double, startLong: Double, desLat: Double, desLong: Double): List<Int>? {
            val url = "https://api.tomtom.com/routing/1/calculateRoute/$startLat,$startLong:$desLat,$desLong/json?key=AOYMhs1HWBhlfnU4mIaiSULFfvNGTw4Z&travelMode=pedestrian"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .build()
            var routeLength = 0
            var routeTime = 0

            val executor = Executors.newSingleThreadExecutor()

            val task: Callable<List<Int>> = Callable<List<Int>> {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseData = response.body?.string()
                    if (responseData != null) {
                        val routeResponse = parseRouteData(responseData)
                        routeResponse.routes.forEach { route ->
                            routeLength = route.summary.lengthInMeters
                            routeTime = (route.summary.travelTimeInSeconds/60.0).roundToInt()
                        }
                        return@Callable listOf(routeLength, routeTime)
                    }
                    throw IllegalStateException("Response Data is Null!")
                }
            }

            val future = executor.submit(task)
            val results: List<Int>? = try {
                future.get()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                executor.shutdown()
            }

            return results
        }

        private fun parseRouteData(jsonData: String): HomeFragment.RouteResponse {
            val gson = Gson()
            return gson.fromJson(jsonData, HomeFragment.RouteResponse::class.java)
        }


        @SuppressLint("SetTextI18n")
        fun bind(location: test) {
            val parts = location.Coordinates.split(',')
            val longitude: Double = parts[0].toDouble()
            val latitude: Double = parts[1].toDouble()

            val calculations = calcRange(
                42.350498333333334,
                -71.10539833333333,
                latitude,
                longitude
            )

            addressTextView.text = location.Location
            distanceTextView.text = "${calculations?.get(0)}m"
            timeTextView.text = "${calculations?.get(1)} min"
        }
    }
}

