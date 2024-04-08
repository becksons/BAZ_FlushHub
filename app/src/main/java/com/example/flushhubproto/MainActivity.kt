package com.example.flushhubproto

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.tomtom.R
import com.example.tomtom.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var topDrawer: FrameLayout
    private var isDrawerOpen = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)




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



    override fun onStart() {
        super.onStart()

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        topDrawer = findViewById(R.id.topDrawer)


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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }
}
