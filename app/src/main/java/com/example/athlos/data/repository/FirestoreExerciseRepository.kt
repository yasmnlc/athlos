package com.example.athlos.data.repository

import com.example.athlos.ui.models.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreExerciseRepository(
    private val firestore: FirebaseFirestore
) : ExerciseRepository {

    private val EXERCISES_COLLECTION = "exercises"

    override suspend fun getExerciseById(id: String): Exercise? {
        return try {
            firestore.collection(EXERCISES_COLLECTION).document(id).get().await()
                .toObject(Exercise::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveExercise(exercise: Exercise) {
        try {
            exercise.id?.let {
                firestore.collection(EXERCISES_COLLECTION).document(it).set(exercise).await()
            }
        } catch (e: Exception) {
            // Log the error or handle it as appropriate for your application
        }
    }
}