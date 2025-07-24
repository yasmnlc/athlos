package com.example.athlos.ui.models

import com.google.firebase.firestore.DocumentId

data class CustomWorkout(
    @DocumentId val id: String = "",
    val name: String = "",
    val exerciseIds: List<String> = emptyList(),
    val userId: String = "",
    val bodyParts: List<String> = emptyList()
)