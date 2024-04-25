package com.example.flushhubproto.ui.rating

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DummyReviewsViewModel : ViewModel() {

    private val _ratings = MutableLiveData<List<Restroom>>()
    val ratings: LiveData<List<Restroom>> = _ratings

    init {
        loadRatings()
    }

    private fun loadRatings() {

        _ratings.value = listOf(
//Asked chatGPT to create more dummy reviews
            Restroom(id = 2, name = "Restroom A",floor =  1, building = "CDS",gender = 0, rating =  3.5f, reviews = emptyList()),
            Restroom(id = 3, name ="Restroom B", floor = 2, building = "MSC", rating= 4.5f ,gender = 2,reviews = emptyList()),
            Restroom(id = 4 , name ="Restroom C", floor =3, building = "CAS", rating= 2.0f,gender = 1, reviews = emptyList()),
            Restroom(id = 5 , name ="Restroom D", floor =2, building = "CAS", rating= 2.5f,gender = 0, reviews = emptyList()),
            Restroom(id = 6, name = "Restroom E", floor = 1, building = "MSC", gender = 0, rating = 3.0f, reviews = emptyList()),
            Restroom(id = 7, name = "Restroom F", floor = 1, building = "CDS", gender = 1, rating = 3.8f, reviews = emptyList()),
            Restroom(id = 8, name = "Restroom G", floor = 2, building = "MSC", gender = 2, rating = 4.0f, reviews = emptyList()),
            Restroom(id = 9, name = "Restroom H", floor = 4, building = "CAS", gender = 1, rating = 1.5f, reviews = emptyList()),
            Restroom(id = 10, name = "Restroom I", floor = 2, building = "CDS", gender = 0, rating = 4.2f, reviews = emptyList()),
            Restroom(id = 11, name = "Restroom J", floor = 3, building = "MSC", gender = 2, rating = 5.0f, reviews = emptyList()),
            Restroom(id = 12, name = "Restroom K", floor = 3, building = "CAS", gender = 0, rating = 2.3f, reviews = emptyList()),
            Restroom(id = 13, name = "Restroom L", floor = 4, building = "CDS", gender = 1, rating = 3.7f, reviews = emptyList()),
            Restroom(id = 14, name = "Restroom M", floor = 1, building = "MSC", gender = 2, rating = 4.8f, reviews = emptyList())

        )
    }
}