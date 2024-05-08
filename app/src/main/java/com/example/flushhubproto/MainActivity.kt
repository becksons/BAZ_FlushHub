package com.example.flushhubproto

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
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
import com.example.flushhubproto.ui.home.HomeFragment.Companion.REQUEST_LOCATION_PERMISSION
import com.example.tomtom.R
import com.example.tomtom.databinding.ActivityMainBinding
import com.tomtom.sdk.location.LocationProvider
import io.realm.Realm


class MainActivity : AppCompatActivity() {
    companion object {
        var isInitLoading = MutableLiveData(true)
        var swipeLoading = MutableLiveData(false)
        var isQueryLoading = MutableLiveData(false)
        var isRealmInit = MutableLiveData(false)
        var queryEmpty = true
        var loadStart = true
        var currentLongitude: Double = 0.0
        var currentLatitude: Double = 0.0
        var currentBathroom: Triple<bathroom, Double, Double>? = null

    }

    private var androidLocationProvider: LocationProvider? = null

    private lateinit var binding: ActivityMainBinding
    private lateinit var topDrawer: FrameLayout

    private var isDrawerOpen = false
    private var submitButton:Button? = null
    private var nameEditText :EditText? = null
    private var landingPage :LinearLayout? = null
    private lateinit var navController: NavController
    private lateinit var greetingTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var bathroomViewModel: BathroomViewModel

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionsIfNecessary()
        Realm.init(this) // DB initialization
        bathroomViewModel = ViewModelProvider(this)[BathroomViewModel::class.java]

        // Get users current location and set the location (once)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            Log.d("INIT", "User Already Gave Existing Permissions!")
            val lm = getSystemService(LOCATION_SERVICE) as LocationManager
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("INIT", "Permissions Checked. Allowed. Fetching Current Location...")
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    Long.MAX_VALUE,
                    Float.MAX_VALUE,
                    locationListener
                )
            } else {
                Log.d("INIT", "Permissions Checked. User Denied. Fetching Default Location...")
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show()
                currentLatitude = 42.3505
                currentLongitude = -71.1054
                isRealmInit.postValue(true)

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                Log.d("INIT", "Getting Request Code...")
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val lm = getSystemService(LOCATION_SERVICE) as LocationManager
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d("INIT", "Permissions Checked. Allowed. Fetching Current Location...")
                        lm.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            Long.MAX_VALUE,
                            Float.MAX_VALUE,
                            locationListener
                        )
                    }
                } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    currentLatitude = 42.3505
                    currentLongitude = -71.1054
                    isRealmInit.postValue(true)
                }
            }
        }
    }

    // One time location listener
    private val locationListener: LocationListener = LocationListener { location ->
        currentLatitude = location.latitude
        currentLongitude = location.longitude
        Log.d("INIT", "Fetched Current Location. Posting Value!")
        if (currentLatitude == 0.0 || currentLongitude == 0.0)
        {
            Log.d("INIT", "Location Listener Failed to get proper location! Setting Defaults...")
            currentLatitude = 42.3505
            currentLongitude = -71.1054
        }

        isRealmInit.postValue(true)
    }

    private fun setupDrawer() {
        topDrawer = findViewById(R.id.topDrawer)
        navController = findNavController(R.id.nav_host_fragment_content_main)

        // Kickstart Loading Screen
        isInitLoading.observe(this) { isLoading ->
            if (isLoading) {
                Log.d("INIT", "Initializing...")
                navController.navigate(R.id.initLoadingFragment)
                binding.root.isClickable = false
                binding.appBarMain.appBarBanner.visibility = GONE
                binding.appBarMain.openDrawerButton.visibility = GONE
                binding.appBarMain.menuText.visibility = GONE
            } else if (loadStart) {
                binding.appBarMain.appBarBanner.visibility =  VISIBLE
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
                        text =  getString(R.string.find_your_restroom)
                        textSize = 23.0F
                        translationX = 2.0F
                        translationY = 10.0F
                        gravity = Gravity.CENTER
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
                R.id.randomAnimalFragment -> {
                    fragmentBannerView.visibility = VISIBLE
                    fragmentBannerView.apply {
                        text = getString(R.string.entertainment_banner)
                        textSize = 24.0F
                        translationX = 2.0F
                        translationY = 10.0F
                        gravity = Gravity.CENTER
                    }
                    greetingTextView.visibility = GONE
                    distanceTextView.visibility = GONE

                }
                R.id.flushHubBoggle ->{
                    fragmentBannerView.visibility = VISIBLE
                    fragmentBannerView.apply {
                        text =getString(R.string.entertainment_banner)
                        textSize = 24.0F
                        translationX = 2.0F
                        translationY = 10.0F
                        gravity = Gravity.CENTER

                    }
                    greetingTextView.visibility = GONE
                    distanceTextView.visibility = GONE

                }
                R.id.nav_entertainment ->{
                    fragmentBannerView.visibility = VISIBLE
                    fragmentBannerView.apply {
                        text = getString(R.string.entertainment_banner)
                        textSize = 24.0F
                        translationX = 2.0F
                        translationY = 10.0F
                        gravity = Gravity.CENTER

                    }
                    greetingTextView.visibility = GONE
                    distanceTextView.visibility = GONE

                }
                R.id.newsListFragment ->{
                    fragmentBannerView.visibility = VISIBLE
                    fragmentBannerView.apply {
                        text = getString(R.string.news_banner)
                        textSize = 24.0F
                        translationX = 2.0F
                        translationY = 10.0F
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
       isQueryLoading.observe(this) { queryLoading ->
            if (queryLoading) {
                navController.navigate(R.id.queryLoadingFragment)
                binding.root.isClickable = false
                binding.appBarMain.appBarBanner.visibility = GONE

                binding.appBarMain.openDrawerButton.visibility = GONE
                binding.appBarMain.menuText.visibility = GONE
            } else if (!loadStart) {
                binding.appBarMain.appBarBanner.visibility =  VISIBLE

                binding.appBarMain.openDrawerButton.visibility = VISIBLE
                binding.appBarMain.menuText.visibility = VISIBLE
                binding.root.isClickable = true

                if (!queryEmpty) {
                    navController.navigate(R.id.queryResultFragment)
                    queryEmpty = true
                } else {
                    Toast.makeText(this, getString(R.string.query_not_found), Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.nav_home)
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
        distanceTextView.textAlignment = View.TEXT_ALIGNMENT_INHERIT

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
