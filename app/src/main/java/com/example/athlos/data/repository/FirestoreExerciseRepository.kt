package com.example.athlos.data.repository

import com.example.athlos.ui.models.Exercise
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log

class FirestoreExerciseRepository(
    private val firestore: FirebaseFirestore
) : ExerciseRepository {

    private val EXERCISES_COLLECTION = "exercises"

    override suspend fun getExerciseById(id: String): Exercise? {
        return try {
            firestore.collection(EXERCISES_COLLECTION).document(id).get().await()
                .toObject(Exercise::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Erro ao buscar exercício por ID: $id", e)
            null
        }
    }

   override suspend fun getExercisesByBodyPart(bodyPart: String): List<Exercise> {
        return try {
            val querySnapshot = firestore.collection(EXERCISES_COLLECTION)
                .whereEqualTo("bodyPart", bodyPart)
                .get()
                .await()
            querySnapshot.toObjects(Exercise::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Erro ao buscar exercícios por bodyPart: $bodyPart", e)
            emptyList()
        }
    }

    override suspend fun saveExercise(exercise: Exercise) {
        try {
            exercise.id?.let {
                firestore.collection(EXERCISES_COLLECTION).document(it).set(exercise).await()
            }
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Erro ao salvar exercício: ${exercise.id}", e)
        }
    }
}