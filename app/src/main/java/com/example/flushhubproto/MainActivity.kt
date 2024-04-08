package com.example.flushhubproto
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.GestureDetector
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flushhubproto.ui.home.HomeFragment.Companion.REQUEST_LOCATION_PERMISSION
import com.example.flushhubproto.ui.home.HomeViewModel

import com.example.tomtom.R
import com.example.tomtom.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var topDrawer: FrameLayout
    private lateinit var viewModel: HomeViewModel
    private lateinit var recyclerView: RecyclerView


    private var isDrawerOpen = false
    private var greetBuilder : StringBuilder? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
            return
        }
        val drawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.home_icon)
        drawable?.setBounds(0, 0, (drawable.intrinsicWidth * 0.5).toInt(), (drawable.intrinsicHeight * 0.5).toInt()) // Scale drawable to half its size
        binding.btnNavHome.setCompoundDrawables(drawable, null, null, null)

        val mainLayout: ViewGroup = findViewById(R.id.main_layout)
        mainLayout.setOnTouchListener { _, event ->
            if (isDrawerOpen && !isTouchInsideView(event, topDrawer)) {
                closeTopDrawer()
                return@setOnTouchListener true
            }
            false
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)

        } else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Use the location object, which could be null in rare cases
                    location?.let {
                        val latitude = it.latitude
                        val longitude = it.longitude


                    }
                }
        }
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        recyclerView = findViewById(R.id.nearest_location_recycler_view)
        val adapter = LocationInfoAdapter(emptyList())

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

    }






private fun isTouchInsideView(event: MotionEvent, view: View): Boolean {
//    Checking if touch event in range to close top drawer
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
    override fun onStart() {
        super.onStart()

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        topDrawer = findViewById(R.id.topDrawer)


        greetUser("User", 0.1F)


        val openButton: ImageButton = findViewById(R.id.open_drawer_button)
        openButton.setOnClickListener {
            if (isDrawerOpen) {
                closeTopDrawer()
            } else {
                openTopDrawer()
            }
        }
        binding.menuButton.setOnClickListener {
            navController.navigate(R.id.nav_home)
            closeTopDrawer()

        }
        binding.btnNavHome.setOnClickListener{
            navController.navigate(R.id.nav_home)
            closeTopDrawer()

        }
        binding.btnNavFind.setOnClickListener{
            navController.navigate(R.id.nav_gallery)
            closeTopDrawer()

        }
        binding.btnNavRate.setOnClickListener{
            navController.navigate(R.id.nav_slideshow)
            closeTopDrawer()

        }
    }
    private fun greetUser(name: String,milesAway: Float){
        Log.d("User greet text", "User greeted")
        val greetingTextView: TextView = findViewById(R.id.greeting_text_name)
        val distanceTextView: TextView = findViewById(R.id.greeting_miles_away)


        val greetingText = SpannableString("Don't worry, $name!")
        greetingText.setSpan(StyleSpan(Typeface.BOLD), 12, 12 + name.length+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val distanceText = SpannableString("The nearest restroom is \n $milesAway miles away!")
        distanceText.setSpan(StyleSpan(Typeface.BOLD), 24, 24 + milesAway.toInt()+9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)


        greetingTextView.text = greetingText
        distanceTextView.text = distanceText


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





    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
}
