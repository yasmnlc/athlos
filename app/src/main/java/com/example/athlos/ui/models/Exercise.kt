package com.example.athlos.ui.models

data class Exercise(
    val id: String,
    val name: String?,
    val target: String?,
    val equipment: String?,
    val gifUrl: String?,
    val bodyPart: String?,
    var videoId: String? = null
)