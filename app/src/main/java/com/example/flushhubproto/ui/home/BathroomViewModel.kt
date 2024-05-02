package com.example.flushhubproto.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flushhubproto.MainActivity
import com.example.flushhubproto.schema.bathroom
import com.google.gson.Gson
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.sync.Subscription
import io.realm.mongodb.sync.SyncConfiguration
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class BathroomViewModel : ViewModel() {
    private lateinit var app: App
    private var realm: Realm? = null
    private val _selectedLocation = MutableLiveData<String>()
    val selectedLocation: LiveData<String> = _selectedLocation

    //Search query listener
    private val _queryReady = MutableLiveData<Boolean>(false)
    val queryReady :MutableLiveData<Boolean> get() = _queryReady

    private val _searchQuery = MutableLiveData<Triple<String,String,String>>()
    val searchQuery: MutableLiveData<Triple<String,String,String>>get() = _searchQuery

    // Data vars
    private val _bathrooms = MutableLiveData<List<Triple<bathroom, Double, Double>>?>()
    val bathrooms: MutableLiveData<List<Triple<bathroom, Double, Double>>?> get() = _bathrooms

    private val _queriedBathrooms = MutableLiveData<List<Triple<bathroom, Double, Double>>?>()
    val queriedBathrooms: MutableLiveData<List<Triple<bathroom, Double, Double>>?> get()  = _queriedBathrooms

    init {
        initializeMongoDBRealm()
    }
    fun selectLocation(address: String) {
        _selectedLocation.value = address
    }



    private fun initializeMongoDBRealm() {
        app = App(AppConfiguration.Builder("flushhub-etqha").build()) // Initialize App

        // Login
        app.loginAsync(Credentials.anonymous()) { result ->
            if (result.isSuccess) {
                setupRealm()
            } else {
                Log.e("FlUSHHUB", "Failed Login: ${result.error}")
            }
        }
    }

    private fun setupRealm() {
        val user = app.currentUser() ?: throw IllegalStateException("MongoDB User Not Logged In!")
        Log.i("FlUSHHUB", "Logged in as: ${user}")

        val flexibleSyncConfig = SyncConfiguration.Builder(user)
            .initialSubscriptions { realm, subscriptions ->
                if (subscriptions.find("all-bathrooms") == null) {
                    subscriptions.add(
                        Subscription.create(
                            "all-bathrooms",
                            realm.where(bathroom::class.java)
                        )
                    )
                }
            }
            .build()

        realm = try {
            Realm.getInstance(flexibleSyncConfig)
        } catch (e: Exception) {
            null
        }

        loadAllBathrooms()
    }

    // ================== Database Processing Functions ==================

    // Calculates Distance and Travel time based on given Coordinates
    private fun calcRange(startLat: Double, startLong: Double, desLat: Double, desLong: Double): List<Int>? {
        val apiKey = "YbAIKDlzANgswfBTirAdDONIKfLN9n6J"
        val url = "https://api.tomtom.com/routing/1/calculateRoute/$startLat,$startLong:$desLat,$desLong/json?key=$apiKey&travelMode=pedestrian"
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

    private fun loadAllBathrooms() {
        realm?.executeTransactionAsync { bgRealm ->
            val results = bgRealm.where(bathroom::class.java)?.findAll()
            val bathrooms = results?.let { bgRealm.copyFromRealm(it) }
            if (bathrooms != null) { // Do a null check, if null we don't proceed
                _bathrooms.postValue(bathrooms.map { test -> // Critical Thread processes! Crashes may happen here if resources are not correctly allocated
                    // Defaults
                    var distance = -1.0
                    var time = -1.0

                    val parts = test.Coordinates.split(',')
                    val longitude: Double = parts[0].toDouble()
                    val latitude: Double = parts[1].toDouble()

                    val calculations = calcRange(
                        42.350498333333334,
                        -71.10539833333333,
                        latitude,
                        longitude
                    )

                    if (calculations != null) {
                        distance = calculations[0].toDouble()
                        time = calculations[1].toDouble()
                    }

                    Log.i("FlUSHHUB", "Triple Created: ${Triple(test, distance, time)}")

                    Triple(test, distance, time)
                }.sortedBy { if (it.second == -1.0) Double.MAX_VALUE else it.second })
            }
            MainActivity.isLoading.postValue(false) // Finish Loading
        }
    }
    fun queryBathroomsFullQuery(gender: String, area: String, minRating: Double, currLat: Double, currLong: Double) {
        realm?.executeTransactionAsync { bgRealm ->
            val results = bgRealm.where(bathroom::class.java)
            .equalTo("Type", gender)
            .greaterThanOrEqualTo("Rating", minRating)
            .findAll()

            val queryResults = results?.let { bgRealm.copyFromRealm(it) }
            if (!queryResults.isNullOrEmpty()) {
                _queriedBathrooms.postValue(queryResults.mapNotNull { queryRes->
                    // Defaults
                    var distance = -1.0
                    var time = -1.0

                    val parts = queryRes.Coordinates.split(',')
                    val longitude: Double = parts[0].toDouble()
                    val latitude: Double = parts[1].toDouble()

                    Log.i("FlUSHHUB", "[QUERY] Got locations of: ${queryRes.Location}")

                    if(area == "west" && longitude < -71.110940) {
                        Log.i("FlUSHHUB", "[QUERY] CALCULATING WEST")
                        val calculations = calcRange(
                            currLat,
                            currLong,
                            latitude,
                            longitude
                        )

                        if (calculations != null) {
                            distance = calculations[0].toDouble()
                            time = calculations[1].toDouble()
                        }
                    } else if(area == "central" && longitude >= -71.110940 && longitude <= -71.100546){
                        Log.i("FlUSHHUB", "[QUERY] CALCULATING CENTRAL")
                        val calculations = calcRange(
                            currLat,
                            currLong,
                            latitude,
                            longitude
                        )

                        if (calculations != null) {
                            distance = calculations[0].toDouble()
                            time = calculations[1].toDouble()
                        }
                    } else if(area == "east" && longitude > -71.100546){
                        Log.i("FlUSHHUB", "[QUERY] CALCULATING EAST")
                        val calculations = calcRange(
                            currLat,
                            currLong,
                            latitude,
                            longitude
                        )

                        if (calculations != null) {
                            distance = calculations[0].toDouble()
                            time = calculations[1].toDouble()
                        }
                    }

                    if (distance != -1.0 && time != -1.0) {
                        Log.i("FlUSHHUB", "[QUERY] Triple Created: ${Triple(queryRes, distance, time)}")
                        Triple(queryRes, distance, time)
                    } else {
                        null
                    }
                }.sortedBy { it.second })
            }
            MainActivity.isLoading.postValue(false) // Finish Loading
        }
    }
    // ===================================================================
    override fun onCleared() {
        realm?.close()
        realm = null
        super.onCleared()
    }

    fun updateSelectedLocation(locationInfo: String) {
        _selectedLocation.value = locationInfo
    }
}