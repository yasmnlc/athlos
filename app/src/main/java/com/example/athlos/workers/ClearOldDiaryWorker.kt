package com.example.athlos.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.athlos.data.local.AppDatabase
import com.example.athlos.data.repository.DiaryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

class ClearOldDiaryWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("ClearOldDiaryWorker", "Iniciando tarefa de limpeza do diário local.")
        return try {
            // 1. Obtenha as instâncias necessárias
            val foodDao = AppDatabase.getDatabase(applicationContext).foodEntryDao()
            val firestore = FirebaseFirestore.getInstance() // ADICIONADO
            val auth = FirebaseAuth.getInstance()       // ADICIONADO

            // 2. Crie o repositório com todas as dependências
            val repository = DiaryRepository(foodDao, firestore, auth) // ATUALIZADO

            // 3. Execute a tarefa de limpeza
            repository.clearOldEntries()
            Log.d("ClearOldDiaryWorker", "Entradas antigas do diário local foram limpas com sucesso.")
            Result.success()
        } catch (e: Exception) {
            Log.e("ClearOldDiaryWorker", "Falha ao limpar o diário local.", e)
            Result.failure()
        }
    }
}