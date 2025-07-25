package com.example.athlos.data.repository

import android.content.Context
import android.util.Log
import com.example.athlos.data.model.User
import com.example.athlos.ui.models.CustomWorkout
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.example.athlos.utils.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.reflect.KProperty1
import com.google.firebase.firestore.SetOptions

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
    suspend fun signInWithCredential(credential: AuthCredential): FirebaseUser?
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun updateUserData(userId: String, data: Map<String, Any>, merge: Boolean)
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
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
            goal = "",
            aguaAtual = 0,
            aguaMeta = 2000,
            lastResetDate = LocalDate.MIN.format(DateTimeFormatter.ISO_LOCAL_DATE),
            profileImageUrl = null,
            savedWorkouts = emptyList()
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
        updateUserData(uid, updates, merge = false)
    }

    override suspend fun updateUserData(userId: String, data: Map<String, Any>, merge: Boolean) {
        if (merge) {
            // .set com SetOptions.merge():
            // Cria o documento se ele não existir.
            // Se o documento existir, apenas atualiza os campos do 'data', sem apagar os outros.
            // É EXATAMENTE o que precisamos para o login com Google.
            firestore.collection("users").document(userId)
                .set(data, SetOptions.merge()).await()
        } else {
            // .update():
            // Apenas atualiza os campos de um documento que já deve existir.
            // Falha se o documento não existir. Mantém o comportamento original.
            firestore.collection("users").document(userId)
                .update(data).await()
        }
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

    override suspend fun signInWithCredential(credential: AuthCredential): FirebaseUser? {
        val userCredential = auth.signInWithCredential(credential).await()
        return userCredential.user
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await() // Chama a função do Firebase Auth
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