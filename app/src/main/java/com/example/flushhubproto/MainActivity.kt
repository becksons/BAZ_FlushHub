package com.example.flushhubproto

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.flushhubproto.schema.bathroom
import com.example.flushhubproto.ui.home.BathroomViewModel
import com.example.flushhubproto.ui.home.HomeFragment
import com.example.flushhubproto.ui.home.HomeFragment.Companion.REQUEST_LOCATION_PERMISSION
import com.example.tomtom.R
import com.example.tomtom.databinding.ActivityMainBinding
import com.tomtom.quantity.Distance
import com.tomtom.sdk.location.GeoLocation
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.android.AndroidLocationProvider
import com.tomtom.sdk.location.android.AndroidLocationProviderConfig
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import io.realm.Realm
import kotlin.time.Duration.Companion.milliseconds


class MainActivity : AppCompatActivity() {
    companion object {
        var isLoading = MutableLiveData(true)
        var swipeLoading = MutableLiveData(false)
        var isQueryLoading = MutableLiveData(false)
        var isRealmInit = MutableLiveData(0)
        private val mapOptions = MapOptions(mapKey ="YbAIKDlzANgswfBTirAdDONIKfLN9n6J")
        val mapFragment = MapFragment.newInstance(mapOptions)
        var currentLongitude: Double? = 0.0
        var currentLatitude: Double? = 0.0
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var topDrawer: FrameLayout
    private var loadStart = true

    private var isDrawerOpen = false
    private var submitButton:Button? = null
    private var nameEditText :EditText? = null
    private var landingPage :LinearLayout? = null
    private var androidLocationProvider: LocationProvider? = null
    private lateinit var navController: NavController
    private lateinit var greetingTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var bathroomViewModel: BathroomViewModel

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Map Init Routine
        requestPermissionsIfNecessary()
        androidLocationProvider = AndroidLocationProvider(
            context = this,
            config = androidLocationProviderConfig
        )
        Realm.init(this) // DB initialization
        bathroomViewModel = ViewModelProvider(this)[BathroomViewModel::class.java]

        // Get users current location and set the location (once)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isQueryLoading.postValue(false)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
            return
        }

        val mainLayout: ViewGroup = binding.mainLayout

        mainLayout.setOnTouchListener { _, event ->
            if (isDrawerOpen && !isTouchInsideView(event, topDrawer)) {
                closeTopDrawer()
                return@setOnTouchListener true
            }
            false
        }
    }

    override fun onStart() {
        super.onStart()
        setupDrawer()

        submitButton = findViewById<Button>(R.id.submitButton)
        nameEditText = findViewById<EditText>(R.id.nameEditText)
        landingPage = findViewById<LinearLayout>(R.id.landing_layout)
        submitButton?.setOnClickListener {
            val name = nameEditText?.text.toString()
            if (name.isNotEmpty()) {
                saveName(name)
                greetUser(name,0.01F)
                landingPage?.visibility = GONE
            } else {
                Toast.makeText(this, getString(R.string.ask_name), Toast.LENGTH_SHORT).show()
            }
        }


        checkAndDisplayUserName()
        val openButton: ImageButton = findViewById(R.id.open_drawer_button)
        openButton.setOnClickListener {
            if (isDrawerOpen) {
                closeTopDrawer()
            } else {
                openTopDrawer()
            }
        }

    }
    private fun requestPermissionsIfNecessary() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            initializeMapWithLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeMapWithLocation()
                    val lm = getSystemService(LOCATION_SERVICE) as LocationManager
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        lm.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            Long.MAX_VALUE,
                            Float.MAX_VALUE,
                            locationListener
                        )
                    }
                } else {
                    //TODO: error handle for permission denied
                }
            }
        }
    }

    // One time location listener
    private val locationListener: LocationListener = LocationListener { location ->
        currentLongitude = location.longitude
        currentLatitude = location.latitude
        isRealmInit.postValue(2)
    }


    private val androidLocationProviderConfig = AndroidLocationProviderConfig(
        minTimeInterval = 1000.milliseconds,
        minDistance = Distance.meters(10.0)
    )

    private fun initializeMapWithLocation() {
        mapFragment.getMapAsync { tomtomMap ->
            tomtomMap.setLocationProvider(androidLocationProvider)
            androidLocationProvider?.enable()

            val onLocationUpdateListener = OnLocationUpdateListener { location: GeoLocation ->
                Log.d("Location Update", "Latitude: ${location.position.latitude}, Longitude: ${location.position.longitude}")

                HomeFragment.currentLatitude = location.position.latitude
                HomeFragment.currentLongitude = location.position.longitude

                moveMap(tomtomMap, location.position.latitude, location.position.longitude)
                updateUserLocationOnMap(tomtomMap,location.position.latitude,location.position.longitude)
            }

            androidLocationProvider?.addOnLocationUpdateListener(onLocationUpdateListener)
        }
    }

    private fun moveMap(tomtomMap: TomTomMap, lat: Double, long: Double){
        val cameraOptions = CameraOptions(
            position = GeoPoint(lat, long),
            zoom = 17.0,
            tilt = 0.0,
            rotation = 0.0
        )

        tomtomMap.moveCamera(cameraOptions)
    }

    private fun updateUserLocationOnMap(tomtomMap: TomTomMap, lat: Double, long: Double) {
        val locationMarkerOptions = LocationMarkerOptions(
            type = LocationMarkerOptions.Type.Chevron
        )

        tomtomMap.enableLocationMarker(locationMarkerOptions)
    }

    private fun setupDrawer() {
        topDrawer = findViewById(R.id.topDrawer)
        navController = findNavController(R.id.nav_host_fragment_content_main)

        // Kickstart Loading Screen
        isLoading.observe(this) { isLoading ->
            if (isLoading) {
                navController.navigate(R.id.initLoadingFragment)
                binding.root.isClickable = false
                binding.appBarMain.appBarBanner.visibility = GONE
                binding.appBarMain.navHeaderMain.root.visibility = GONE
                binding.appBarMain.openDrawerButton.visibility = GONE
                binding.appBarMain.menuText.visibility = GONE
            } else {
                binding.appBarMain.appBarBanner.visibility =  VISIBLE
                binding.appBarMain.navHeaderMain.root.visibility = VISIBLE
                binding.appBarMain.openDrawerButton.visibility = VISIBLE
                binding.appBarMain.menuText.visibility = VISIBLE

                navController.navigate(R.id.nav_home)
                binding.root.isClickable = true
                loadStart = false
            }
        }
        observeLoadingState()


        val btnMenuClose: ImageButton = findViewById(R.id.menu_button_close)
        val fragmentBannerView:TextView = findViewById(R.id.fragment_banner)
        val btnNavHome: Button = findViewById(R.id.btn_nav_home)
        val btnNavFind: Button = findViewById(R.id.btn_nav_find)
        val btnNavRate: Button = findViewById(R.id.btn_nav_rate)
        val btnNavEntertainment: Button = findViewById(R.id.btn_nav_entertainment)
        val drawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.home_icon)
        drawable?.setBounds(0, 0, (drawable.intrinsicWidth * 0.5).toInt(), (drawable.intrinsicHeight * 0.5).toInt())
        btnNavHome.setCompoundDrawables(drawable, null, null, null)

        btnMenuClose.setOnClickListener {
            closeTopDrawer()
        }

        btnNavEntertainment.setOnClickListener{
            val currentDestinationId = navController.currentDestination?.id
            if(currentDestinationId!=R.id.nav_entertainment){
                navController.navigate(R.id.nav_entertainment)
                closeTopDrawer()

            }else{
                closeTopDrawer()
            }
        }

        btnNavHome.setOnClickListener {
            val currentDestinationId = navController.currentDestination?.id
            if(currentDestinationId!=R.id.nav_home){
                navController.navigate(R.id.nav_home)
                closeTopDrawer()
            }else{
                closeTopDrawer()
            }
        }
        btnNavFind.setOnClickListener {
            val currentDestinationId = navController.currentDestination?.id
            if(currentDestinationId!=R.id.nav_gallery){
                navController.navigate(R.id.nav_gallery)
                closeTopDrawer()
            }else{
                closeTopDrawer()
            }
        }
        btnNavRate.setOnClickListener {
            val currentDestinationId = navController.currentDestination?.id
            if(currentDestinationId!=R.id.nav_slideshow){
                navController.navigate(R.id.nav_slideshow)
                closeTopDrawer()
            }else{
                closeTopDrawer()
            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_gallery -> {
                    fragmentBannerView.visibility = VISIBLE
                    fragmentBannerView.apply {
                        text = getString(R.string.find_your_restroom)
                        textSize = 23.0F
                        translationX = 2.0F
                        translationY = 10.0F
                        gravity = Gravity.CENTER
                    }
                    greetingTextView.visibility = GONE
                    distanceTextView.visibility = GONE
                }

                R.id.nav_slideshow -> {
                    fragmentBannerView.visibility = VISIBLE
                    fragmentBannerView.apply {
                        text = "\n" + getString(R.string.find_your_restroom)
                        textSize = 22.0F
                    }
                    greetingTextView.visibility = GONE
                    distanceTextView.visibility = GONE
                }

                R.id.nav_home -> {
                    greetUserWithDistance()
                    greetingTextView.visibility = VISIBLE
                    distanceTextView.visibility = VISIBLE
                    fragmentBannerView.visibility = GONE
                }
                R.id.nav_entertainment -> {
                    fragmentBannerView.visibility = VISIBLE
                    fragmentBannerView.apply {
                        text = "\n" + getString(R.string.news)
                        textSize = 23.0F
                        translationX = -6.0F
                        translationY= -60.0F
                        gravity = Gravity.CENTER
                    }
                    greetingTextView.visibility = GONE
                    distanceTextView.visibility = GONE
                }

            }
        }
    }
    private fun greetUserWithDistance() {
        val name = getUserName()
        val milesAway = 0.01f  //TODO: Change to meters
        greetUser(name, milesAway)
    }
    private fun getUserName(): String {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("user_name", "User") ?: "User"
    }

    private fun updateBannerText(text: String) {
        greetingTextView = findViewById(R.id.greeting_text_name)
        distanceTextView = findViewById(R.id.greeting_miles_away)
        greetingTextView.text = text
        distanceTextView.visibility = GONE
    }

    private fun isTouchInsideView(event: MotionEvent, view: View): Boolean {
        // Checking if touch event in range to close top drawer
        val viewLocation = IntArray(2)
        view.getLocationOnScreen(viewLocation)
        val viewX = viewLocation[0]
        val viewY = viewLocation[1]
        val viewWidth = view.width
        val viewHeight = view.height

        val touchX = event.rawX.toInt()
        val touchY = event.rawY.toInt()

        return touchX >= viewX && touchX <= (viewX + viewWidth) &&
                touchY >= viewY && touchY <= (viewY + viewHeight)
    }
    private fun observeLoadingState() {
       isQueryLoading.observe(this) { isLoading ->
            if (isLoading) {
                navController.navigate(R.id.initLoadingFragment)
            } else {
                if (navController.currentDestination?.id == R.id.initLoadingFragment && !loadStart) {
                    navController.navigate(R.id.queryResultFragment)
                }
            }
       }
    }
    private fun saveName(name: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("user_name", name)
            putBoolean("is_first_launch", false)
            apply()
        }
    }
    private fun checkAndDisplayUserName() {
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean("is_first_launch", true)
        val name = sharedPreferences.getString("user_name", null)
        if (!isFirstLaunch && name != null) {
            greetUser(name,0.01F)
            landingPage?.visibility = GONE

        } else {
            landingPage?.visibility = VISIBLE

        }
    }


    private fun greetUser(name: String, milesAway: Float) {
        greetingTextView = findViewById(R.id.greeting_text_name)
        distanceTextView = findViewById(R.id.greeting_miles_away)
        greetingTextView.text = getString(R.string.dont_worry) + " $name!"
        distanceTextView.text = "       " + getString(R.string.nearest) + " \n        $milesAway " + getString(R.string.miles_away)

        greetingTextView.visibility = VISIBLE
        distanceTextView.visibility = VISIBLE
    }
    private fun openTopDrawer() {
        topDrawer.animate().translationY(0f).setDuration(300).start()
        isDrawerOpen = true
    }

    private fun closeTopDrawer() {
        topDrawer.animate().translationY(-topDrawer.height.toFloat()).setDuration(300).start()
        isDrawerOpen = false
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isDrawerOpen) {
            closeTopDrawer()
        } else {
            super.onBackPressed()
        }
    }






}
