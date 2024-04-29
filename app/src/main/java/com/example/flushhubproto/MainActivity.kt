package com.example.flushhubproto

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.flushhubproto.ui.home.BathroomViewModel
import com.example.flushhubproto.ui.home.HomeFragment.Companion.REQUEST_LOCATION_PERMISSION

import com.example.tomtom.R
import com.example.tomtom.databinding.ActivityMainBinding
import io.realm.Realm

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var topDrawer: FrameLayout


    private var isDrawerOpen = false
    private var greetBuilder : StringBuilder? = null
    private var submitButton:Button? = null
    private var nameEditText :EditText? = null
    private var landingPage :LinearLayout? = null
    private lateinit var navController: NavController
    private lateinit var greetingTextView: TextView
    private lateinit var distanceTextView: TextView
    private lateinit var fragmentBannerView: TextView

    private val bathroomViewModel: BathroomViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        Realm.init(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION_PERMISSION)
            return
        }

        val mainLayout: ViewGroup = findViewById(R.id.main_layout)

        mainLayout.setOnTouchListener { _, event ->
            if (isDrawerOpen && !isTouchInsideView(event, topDrawer)) {
                closeTopDrawer()
                return@setOnTouchListener true
            }
            false
        }
    }

    private fun setupDrawer() {
        topDrawer = findViewById(R.id.topDrawer)
        navController = findNavController(R.id.nav_host_fragment_content_main)
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
            navController.navigate(R.id.nav_home)
            closeTopDrawer()
        }

        btnNavEntertainment.setOnClickListener{
            navController.navigate(R.id.nav_entertainment)
            closeTopDrawer()

        }

        btnNavHome.setOnClickListener {
            navController.navigate(R.id.nav_home)
            closeTopDrawer()
        }
        btnNavFind.setOnClickListener {
            navController.navigate(R.id.nav_gallery)
            closeTopDrawer()
        }
        btnNavRate.setOnClickListener {
            navController.navigate(R.id.nav_slideshow)
            closeTopDrawer()
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
        //val greetingText = SpannableString(getString(R.string.dont_worry) + " $name!")
        //greetingText.setSpan(StyleSpan(Typeface.BOLD), 12, 12 + name.length+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        //val distanceText = SpannableString("       " + getString(R.string.nearest) + " \n        $milesAway " + getString(R.string.miles_away))
        //distanceText.setSpan(StyleSpan(Typeface.BOLD), 24, 24 + milesAway.toString().length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

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
