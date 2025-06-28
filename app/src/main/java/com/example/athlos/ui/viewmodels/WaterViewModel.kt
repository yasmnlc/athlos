package com.example.athlos.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.data.model.User
import com.example.athlos.data.repository.AuthRepository
import com.example.athlos.data.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WaterUiState(
    val aguaAtual: Int = 0,
    val aguaMeta: Int = 3000, // Valor de teste para a meta de água
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val lastResetDate: String = ""
)

class WaterViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _uiState = MutableStateFlow(WaterUiState())
    val uiState: StateFlow<WaterUiState> = _uiState.asStateFlow()

    init {
        loadWaterData()
    }

    private fun loadWaterData() {
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                try {
                    val userData = authRepository.getUserData(currentUser.uid)
                    val loadedAguaAtual = userData?.aguaAtual ?: 0
                    val loadedAguaMeta = userData?.aguaMeta ?: 2000
                    val loadedLastResetDate = userData?.lastResetDate ?: LocalDate.MIN.format(DateTimeFormatter.ISO_LOCAL_DATE)

                    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

                    var currentWater = loadedAguaAtual
                    if (loadedLastResetDate != today) {
                        currentWater = 0
                        saveWaterData(0, loadedAguaMeta, today)
                        Log.d("WaterViewModel", "Reset diário de água para 0ml. Nova data: $today")
                    }

                    _uiState.value = _uiState.value.copy(
                        aguaAtual = currentWater,
                        aguaMeta = loadedAguaMeta,
                        lastResetDate = today,
                        loading = false
                    )
                    Log.d("WaterViewModel", "Dados de água carregados: $currentWater ml / $loadedAguaMeta ml")

                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Falha ao carregar dados de água: ${e.message}",
                        loading = false
                    )
                    Log.e("WaterViewModel", "Erro ao carregar dados de água: ${e.message}", e)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Nenhum usuário logado.",
                    loading = false
                )
                Log.d("WaterViewModel", "Nenhum usuário logado na WaterScreen.")
            }
        }
    }

    fun addWater(amount: Int) {
        val newAguaAtual = (_uiState.value.aguaAtual + amount).coerceAtMost(_uiState.value.aguaMeta)
        _uiState.value = _uiState.value.copy(aguaAtual = newAguaAtual, successMessage = null)
        saveWaterData(newAguaAtual, _uiState.value.aguaMeta, _uiState.value.lastResetDate)
    }

    fun setWaterGoal(goal: Int) {
        val newGoal = if (goal > 0) goal else 1
        _uiState.value = _uiState.value.copy(aguaMeta = newGoal, successMessage = null)
        saveWaterData(_uiState.value.aguaAtual, newGoal, _uiState.value.lastResetDate)
    }

    private fun saveWaterData(
        currentWater: Int,
        waterGoal: Int,
        lastResetDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    ) {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                try {
                    val updates = mapOf(
                        "aguaAtual" to currentWater,
                        "aguaMeta" to waterGoal,
                        "lastResetDate" to lastResetDate
                    )
                    authRepository.updateUserData(currentUser.uid, updates)
                    _uiState.value = _uiState.value.copy(successMessage = "Dados de água salvos!")
                    Log.d("WaterViewModel", "Dados de água salvos: $currentWater ml, Meta: $waterGoal ml, Data: $lastResetDate")
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Falha ao salvar dados de água: ${e.message}")
                    Log.e("WaterViewModel", "Erro ao salvar dados de água: ${e.message}", e)
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}