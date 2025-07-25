package com.example.athlos.data.repository

import android.util.Log
import com.example.athlos.data.local.FoodEntryDao
import com.example.athlos.data.model.User
import com.example.athlos.ui.models.FoodItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DiaryRepository(
    private val foodEntryDao: FoodEntryDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // AAAA-MM-DD

    // A leitura é sempre feita do banco local (Room) para performance e capacidade offline.
    fun getEntriesForDate(date: LocalDate): Flow<List<FoodItem>> {
        return foodEntryDao.getFoodEntriesForDate(date.format(dateFormatter))
    }

    suspend fun addFoodEntry(foodItem: FoodItem) {
        // 1. Salva localmente (rápido e offline)
        foodEntryDao.insertFoodEntry(foodItem)

        // 2. Sincroniza com o Firebase
        try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("Usuário não está logado.")
            firestore.collection("users").document(userId)
                .collection("diary_entries").document(foodItem.id)
                .set(foodItem).await()
            Log.d("DiaryRepository", "Entrada de alimento ${foodItem.id} sincronizada com o Firestore.")
        } catch (e: Exception) {
            Log.e("DiaryRepository", "Falha ao sincronizar adição com o Firestore: ${e.message}", e)
            // Opcional: Adicionar lógica para tentar sincronizar mais tarde se falhar.
        }
    }

    suspend fun updateFoodEntry(foodItem: FoodItem) {
        // 1. Atualiza localmente
        foodEntryDao.updateFoodEntry(foodItem)

        // 2. Sincroniza a atualização com o Firebase
        try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("Usuário não está logado.")
            firestore.collection("users").document(userId)
                .collection("diary_entries").document(foodItem.id)
                .set(foodItem).await() // 'set' funciona tanto para criar quanto para sobrescrever.
            Log.d("DiaryRepository", "Entrada de alimento ${foodItem.id} atualizada no Firestore.")
        } catch (e: Exception) {
            Log.e("DiaryRepository", "Falha ao sincronizar atualização com o Firestore: ${e.message}", e)
        }
    }

    suspend fun deleteFoodEntry(foodItem: FoodItem) {
        // 1. Apaga localmente
        foodEntryDao.deleteFoodEntry(foodItem)

        // 2. Sincroniza a exclusão com o Firebase
        try {
            val userId = auth.currentUser?.uid ?: throw IllegalStateException("Usuário não está logado.")
            firestore.collection("users").document(userId)
                .collection("diary_entries").document(foodItem.id)
                .delete().await()
            Log.d("DiaryRepository", "Entrada de alimento ${foodItem.id} apagada do Firestore.")
        } catch (e: Exception) {
            Log.e("DiaryRepository", "Falha ao sincronizar exclusão com o Firestore: ${e.message}", e)
        }
    }

    suspend fun clearOldEntries() {
        val yesterday = LocalDate.now().minusDays(1)
        // Esta operação é apenas local, para limpar o cache do dispositivo.
        foodEntryDao.deleteAllOldEntries(yesterday.format(dateFormatter))
    }

    suspend fun getUserData(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return firestore.collection("users").document(uid).get().await()
            .toObject(User::class.java)
    }
}