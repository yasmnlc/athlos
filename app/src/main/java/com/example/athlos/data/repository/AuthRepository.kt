package com.example.athlos.data.repository

import com.example.athlos.data.model.User
import com.example.athlos.utils.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun registerUser(email: String, password: String, userData: Map<String, Any>): FirebaseUser
    suspend fun loginUser(email: String, password: String): FirebaseUser
    fun logoutUser()
    suspend fun getUserData(uid: String): User?
    suspend fun updateUserData(uid: String, updates: Map<String, Any>)
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun registerUser(email: String, password: String, userData: Map<String, Any>): FirebaseUser {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val user = authResult.user ?: throw Exception("Usuário não encontrado após o registro")
        firestore.collection("users").document(user.uid).set(userData).await()
        return user
    }

    override suspend fun loginUser(email: String, password: String): FirebaseUser {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        return authResult.user ?: throw Exception("Usuário não encontrado após o login")
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
}