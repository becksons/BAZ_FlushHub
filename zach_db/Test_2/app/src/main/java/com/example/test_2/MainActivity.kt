package com.example.test_2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity


// Realm MongoDB Base
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration

// Realm Authentication Packages
import io.realm.mongodb.Credentials

// MongoDB Service Packages
import io.realm.mongodb.sync.Subscription
import io.realm.mongodb.sync.SyncConfiguration

// Utility Packages
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize realm
        Realm.init(this)
        val app = App(AppConfiguration.Builder("flushhub-etqha").build()) // Init App

        app.loginAsync(Credentials.anonymous()) { result ->
            if (result.isSuccess) {
                val user = app.currentUser() ?: throw IllegalStateException("User Not Logged In!")
                Log.i("FlUSHHUB", "Logged in as: ${user}")

                val flexibleSyncConfig = SyncConfiguration.Builder(user)
                    .initialSubscriptions { realm, subscriptions ->
                        subscriptions.add(
                            Subscription.create(
                                "all-bathrooms",
                                realm.where(test::class.java)
                            )
                        )
                    }
                    .build()

                val realm : Realm = Realm.getInstance(flexibleSyncConfig)

                // Now we read all of the data
                val bathrooms = realm.where(test::class.java).findAll()
                bathrooms.forEach { bathroom ->
                    Log.d("FLUSHHUB", "Bathroom name: ${bathroom.Name}")
                }


                realm.close() // Close the resources
            } else {
                Log.e("FlUSHHUB", "Failed Login: ${result.error}")
            }
        }


    }
}