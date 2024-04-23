package com.example.flushhubproto.ui.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.flushhubproto.schema.test
import io.realm.Realm
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.sync.Subscription
import io.realm.mongodb.sync.SyncConfiguration

class BathroomViewModel : ViewModel() {
    private lateinit var app: App
    private var realm: Realm? = null

    // Data vars
    private val _bathrooms = MutableLiveData<List<test>?>()
    val bathrooms: MutableLiveData<List<test>?> get() = _bathrooms

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
                            realm.where(test::class.java)
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

    private fun loadAllBathrooms() {
        realm?.executeTransactionAsync { bgRealm ->
            val results = bgRealm.where(test::class.java)?.findAll()
            val bathrooms = results?.let { bgRealm.copyFromRealm(it) }
            _bathrooms.postValue(bathrooms)
        }
    }

    override fun onCleared() {
        realm?.close()
        realm = null
        super.onCleared()
    }
}