package com.example.flushhubproto.ui.rating

data class Restroom(
    val id: Int,
    val name: String,
    val floor: Int,
    val building: String,
    val gender: Int,
    val rating: Float,
    val reviews: List<String>
)

