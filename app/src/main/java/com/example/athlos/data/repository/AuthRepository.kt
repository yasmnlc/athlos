package com.example.athlos.data.repository

import com.example.athlos.data.model.User
import com.example.athlos.ui.models.CustomWorkout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.reflect.KProperty1

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun registerUser(email: String, password: String, userDataMap: Map<String, Any>): FirebaseUser
    suspend fun loginUser(email: String, password: String): FirebaseUser
    fun logoutUser()
    suspend fun getUserData(uid: String): User?
    suspend fun updateUserData(uid: String, updates: Map<String, Any>)
    suspend fun saveCustomWorkout(workout: CustomWorkout)
    suspend fun getCustomWorkouts(): List<CustomWorkout>
    suspend fun deleteCustomWorkout(workoutId: String)
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun registerUser(email: String, password: String, userDataMap: Map<String, Any>): FirebaseUser {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val user = authResult.user ?: throw Exception("Utilizador não encontrado após o registo")

        val initialUser = User(
            uid = user.uid,
            email = email,
            nome = "",
            dataNascimento = "",
            idade = "",
            sexo = "",
            peso = "",
            altura = "",
            praticaExercicios = false,
            diasSemana = "",
            meta = "",
            aguaAtual = 0,
            aguaMeta = 2000,
            lastResetDate = LocalDate.MIN.format(DateTimeFormatter.ISO_LOCAL_DATE),
            profileImageUrl = null,
            favoriteWorkouts = emptyList()
        )

        val finalUserDataToSave = initialUser.toMap().toMutableMap()
        finalUserDataToSave.putAll(userDataMap)

        firestore.collection("users").document(user.uid).set(finalUserDataToSave).await()
        return user
    }

    override suspend fun loginUser(email: String, password: String): FirebaseUser {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        return authResult.user ?: throw Exception("Utilizador não encontrado após o login")
    }

    override fun logoutUser() {
        auth.signOut()
    }

    override suspend fun getUserData(uid: String): User? {
        val document = firestore.collection("users").document(uid).get().await()
        return if (document.exists()) {
            document.toObject(User::class.java)
        } else {
            null
        }
    }

    override suspend fun updateUserData(uid: String, updates: Map<String, Any>) {
        firestore.collection("users").document(uid).update(updates).await()
    }

    override suspend fun saveCustomWorkout(workout: CustomWorkout) {
        val userId = currentUser?.uid ?: throw Exception("Utilizador não autenticado.")
        firestore.collection("users").document(userId)
            .collection("customWorkouts").add(workout.copy(userId = userId)).await()
    }

    override suspend fun getCustomWorkouts(): List<CustomWorkout> {
        val userId = currentUser?.uid ?: return emptyList()
        val snapshot = firestore.collection("users").document(userId)
            .collection("customWorkouts").get().await()
        return snapshot.toObjects(CustomWorkout::class.java)
    }

    override suspend fun deleteCustomWorkout(workoutId: String) {
        val userId = currentUser?.uid ?: throw Exception("Utilizador não autenticado.")
        firestore.collection("users").document(userId)
            .collection("customWorkouts").document(workoutId).delete().await()
    }
}

fun Any.toMap(): Map<String, Any?> {
    return (this as? Map<String, Any?>) ?: run {
        val properties = this::class.members
            .filterIsInstance<KProperty1<Any, *>>()
            .associate { it.name to it.get(this) }
        properties
    }
}