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

    private val _reviewList = MutableLiveData<Map<String, List<String>>>()
    val reviewList: LiveData<Map<String, List<String>>> = _reviewList

    init {
        initializeMongoDBRealm()
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
            Log.i("FlUSHHUB", "COULD NOT GET REALM!")
            null
        }

        Log.d("INIT", "Fetched Realm Instance. Posting Value!")
        MainActivity.isRealmInit.postValue(true)
    }

    // ================== Database Processing Functions ==================

    // Calculates Distance and Travel time based on given Coordinates
    private fun calcRange(startLat: Double, startLong: Double, desLat: Double, desLong: Double): List<Int>? {
        val apiKey = "AOYMhs1HWBhlfnU4mIaiSULFfvNGTw4Z"
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
                //if (!response.isSuccessful) throw IOException("Unexpected code $response") // For Debug

                val responseData = response.body?.string()
                if (responseData != null) {
                    if (responseData != "<h1>Developer Over Qps</h1>" && responseData != "<h1>Call blocked. You went over the allowed limit.</h1>") { // We hit Tom Tom's QPs limit if false
                        val routeResponse = parseRouteData(responseData)
                        routeResponse.routes.forEach { route ->
                            routeLength = route.summary.lengthInMeters
                            routeTime = (route.summary.travelTimeInSeconds/60.0).roundToInt()
                        }
                        return@Callable listOf(routeLength, routeTime)
                    }
                }
//                Log.d("FLUSHHUB", "HIT OVER QPS. DATA IS UNAVALIABLE!") // For Debug
                routeLength = -1 // We return default for the N/A
                routeTime = -1 // We return default for the N/A
                return@Callable listOf(routeLength, routeTime)
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

    fun loadAllBathrooms() {
        realm?.executeTransactionAsync { bgRealm ->
            val results = bgRealm.where(bathroom::class.java)?.greaterThanOrEqualTo("Rating", 0.0) // Done to shorten
                ?.findAll()
            val bathrooms = results?.let { bgRealm.copyFromRealm(it) }
            val reviewMap = mutableMapOf<String, List<String>>()

            if (bathrooms != null) {
                var calculatedAmount = 0
                bathrooms.forEach { bathroom ->
                    val reviews = bathroom.Reviews.split("=").filterNot { it.isBlank() }
                    reviewMap[bathroom._id.toString()] = reviews
                }
                _bathrooms.postValue(bathrooms.map { test -> // Critical Thread processes! Crashes may happen here if resources are not correctly allocated
                    // Defaults
                    var distance = -1.0
                    var time = -1.0

                    val parts = test.Coordinates.split(',')
                    val longitude: Double = parts[0].toDouble()
                    val latitude: Double = parts[1].toDouble()

                    var defaultLat = 42.350498333333334
                    var defaultLong = -71.10539833333333

                    if (MainActivity.currentLongitude != 0.0 && MainActivity.currentLatitude != 0.0) {
                        defaultLat = MainActivity.currentLatitude
                        defaultLong = MainActivity.currentLongitude
                    }

                    val calculations = calcRange(
                        defaultLat,
                        defaultLong,
                        latitude,
                        longitude
                    )

                    if (calculations != null) {
                        distance = calculations[0].toDouble()
                        time = calculations[1].toDouble()
                        if (distance != -1.0 && time != -1.0) {
                            calculatedAmount += 1
                        }
                    }
                    Triple(test, distance, time)
                }.sortedBy { if (it.second == -1.0) Double.MAX_VALUE else it.second })
                Log.d("LOADING ALL BATHROOMS", "Calculated $calculatedAmount / 316 Bathrooms.")
            }
            _reviewList.postValue(reviewMap)  // Post the mapped reviews

            if (MainActivity.isInitLoading.value == true) {
                MainActivity.isInitLoading.postValue(false) // Finish Loading for Initial Loading
            } else if (MainActivity.swipeReviewLoading.value == true) {
                MainActivity.swipeReviewLoading.postValue(false) // Finished Review Swipe Loading
            } else if (MainActivity.swipeBathroomLoading.value == true) {
                MainActivity.swipeBathroomLoading.postValue(false) // Finished Bathroom (Home) Swipe Loading
            }
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

                        Log.i("FlUSHHUB", "[QUERY] Triple Created: ${Triple(queryRes, distance, time)}")
                        Triple(queryRes, distance, time)

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

                        Log.i("FlUSHHUB", "[QUERY] Triple Created: ${Triple(queryRes, distance, time)}")
                        Triple(queryRes, distance, time)

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

                        Log.i("FlUSHHUB", "[QUERY] Triple Created: ${Triple(queryRes, distance, time)}")
                        Triple(queryRes, distance, time)

                    } else if (area == "all") {
                        Log.i("FlUSHHUB", "[QUERY] CALCULATING ALL")
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

                        Log.i("FlUSHHUB", "[QUERY] Triple Created: ${Triple(queryRes, distance, time)}")
                        Triple(queryRes, distance, time)

                    } else {
                        null
                    }

                }.sortedBy { it.second })

                if (!_queriedBathrooms.value.isNullOrEmpty()){
                    MainActivity.queryEmpty = false // We don't have an empty query!
                } else{
                    MainActivity.queryEmpty = true // We do have an empty query
                }
            }
            MainActivity.isQueryLoading.postValue(false)// Finish Loading after gathering Query
        }
    }

    private fun roundToNearestHalf(num: Double): Double {
        return (num * 2).roundToInt() / 2.0
    }

    fun addReview(ratings: Double, restroomName: String, gender: String, floor: String, review: String) {
        realm?.executeTransactionAsync { bgRealm->
            val result = bgRealm.where(bathroom::class.java)
            .equalTo("Name", restroomName)
            .equalTo("Type", gender)
            .findAll()

            val queryResult = result?.let { bgRealm.copyFromRealm(it) }
            if (!queryResult.isNullOrEmpty()) {
                queryResult.map { bathroom ->
                    val previousReviews: String = bathroom.Reviews
                    val currentRating = if (ratings % 1.0 == 0.0) { ratings.toInt() } else { ratings}
                    val currentReview = currentRating.toString() + "$" + "Floor:" + floor + " " + review + "="
                    val previousReviewsList = bathroom.Reviews.split("=")
                    var n = 1 // 1 counting our current review
                    var sum: Double = ratings

                    previousReviewsList.forEach {rev ->
                        if (rev != "") {
                            val x = rev.split("$")
                            sum += x[0].toDouble()
                            n++
                        }
                    }

                    // Now we set the review and the new overall rating
                    result.setDouble("Rating", roundToNearestHalf(sum/n))
                    result.setString("Reviews", previousReviews + currentReview)
                }
            } else {
                Log.d("REVIEW", "Failed to add review!")
            }
        }

    }

    fun test() {
        MainActivity.swipeReviewLoading.postValue(false)
        Log.d("REVIEW", "FETCHED THE DATA!")
    }
    // ===================================================================
    override fun onCleared() {
        realm?.close()
        realm = null
        super.onCleared()
    }
    fun getReviewsForBathroom(bathroomId: String): List<String> {
        return _reviewList.value?.get(bathroomId) ?: emptyList()
    }
    fun updateQueriedBathrooms(results: List<Triple<bathroom, Double, Double>>) {
        _queriedBathrooms.postValue(results)
    }

    fun updateSelectedLocation(locationInfo: String) {
        _selectedLocation.value = locationInfo
    }
}