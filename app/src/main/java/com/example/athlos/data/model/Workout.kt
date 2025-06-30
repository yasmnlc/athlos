package com.example.athlos.data.model

data class Workout(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageRes: Int = 0,
    var isFavorite: Boolean = false
) {
    @Suppress("unused")
    constructor() : this(id = "", title = "", description = "", imageRes = 0, isFavorite = false)
}