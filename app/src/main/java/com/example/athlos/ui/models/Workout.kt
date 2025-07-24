package com.example.athlos.ui.models

data class Workout(
    val id: String,
    val title: String,
    val description: String,
    val imageRes: Int,
    val apiBodyPart: String,
    var isFavorite: Boolean = false
)