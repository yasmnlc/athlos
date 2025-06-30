package com.example.athlos.data.repository

import com.example.athlos.data.model.User
import com.example.athlos.utils.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
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
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun registerUser(email: String, password: String, userDataMap: Map<String, Any>): FirebaseUser {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val user = authResult.user ?: throw Exception("Usuário não encontrado após o registro")

        val initialUser = User(
            uid = user.uid,
            email = email,
            lastResetDate = LocalDate.MIN.format(DateTimeFormatter.ISO_LOCAL_DATE),
            aguaMeta = 2500,
            aguaAtual = 0,
            meta = "",
            profileImageUrl = null
        )

        val initialUserMap = initialUser.toMap().toMutableMap()
        initialUserMap.putAll(userDataMap)

        firestore.collection("users").document(user.uid).set(initialUserMap).await()
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

fun Any.toMap(): Map<String, Any?> {
    return (this as? Map<String, Any?>) ?: run {
        val properties = this::class.members
            .filterIsInstance<KProperty1<Any, *>>()
            .associate { it.name to it.get(this) }
        properties
    }
}