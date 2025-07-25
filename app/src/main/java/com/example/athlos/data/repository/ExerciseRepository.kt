package com.example.athlos.data.repository

import com.example.athlos.ui.models.Exercise

interface ExerciseRepository {
    suspend fun getExerciseById(id: String): Exercise?
    suspend fun getExercisesByBodyPart(bodyPart: String): List<Exercise>
    suspend fun saveExercise(exercise: Exercise)
}