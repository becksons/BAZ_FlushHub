package com.example.flushhubproto.ui.rating

data class Restroom(
    var id: Int,
    val name: String,
    val building: String,
    val floor: Int,
    var rating: Float,
    var gender: Int, // 0: gender neutral 1: womens 2: mens
    val reviews: List<String>
)
