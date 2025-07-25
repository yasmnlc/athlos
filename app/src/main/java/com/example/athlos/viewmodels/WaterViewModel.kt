package com.example.athlos.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.athlos.utils.await
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.athlos.NotificationScheduler
import java.util.Calendar


data class WaterUiState(
    val aguaAtual: Int = 0,
    val aguaMeta: Int = 2000,
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val lastResetDate: String = ""
)

class WaterViewModel(
    application: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

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

                    val pesoUsuario = userData?.peso?.toIntOrNull() ?: 70
                    val metaCalculada = pesoUsuario * 35

                    val loadedAguaAtual = userData?.aguaAtual ?: 0
                    val loadedAguaMeta = userData?.aguaMeta ?: metaCalculada
                    val loadedLastResetDate = userData?.lastResetDate ?: LocalDate.MIN.format(DateTimeFormatter.ISO_LOCAL_DATE)

                    val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

                    var currentWater = loadedAguaAtual
                    var finalAguaMeta = loadedAguaMeta

                    if (loadedLastResetDate != today) {
                        currentWater = 0
                        finalAguaMeta = metaCalculada
                        saveWaterData(0, finalAguaMeta, today)
                        Log.d("WaterViewModel", "Reset diário de água para 0ml. Nova meta: $finalAguaMeta ml. Nova data: $today")
                        scheduleWaterReminder()
                    } else {
                        finalAguaMeta = metaCalculada
                        if (currentWater > finalAguaMeta) {
                            scheduleWaterReminder()
                        } else {
                            cancelWaterReminder()
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        aguaAtual = currentWater,
                        aguaMeta = finalAguaMeta,
                        lastResetDate = today,
                        loading = false
                    )
                    Log.d("WaterViewModel", "Dados de água carregados: $currentWater ml / $finalAguaMeta ml")

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
        val newAguaAtual = _uiState.value.aguaAtual + amount

        if (newAguaAtual > _uiState.value.aguaMeta) {
            cancelWaterReminder()
            _uiState.value = _uiState.value.copy(
                aguaAtual = newAguaAtual,
                successMessage = "Meta diária atingida! Lembretes desativados por hoje."
            )
        } else {
            scheduleWaterReminder()
            _uiState.value = _uiState.value.copy(aguaAtual = newAguaAtual, successMessage = null)
        }

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

    companion object {
        val WATER_REMINDER_NOTIFICATION_ID = NotificationScheduler.CUSTOM_NOTIFICATION_ID_BASE + 1
    }

    // Função para agendar o lembrete de água para a próxima hora
    private fun scheduleWaterReminder() {
        val context = getApplication<Application>().applicationContext
        NotificationScheduler.createNotificationChannel(context)
        val triggerTime = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
        }.timeInMillis

        NotificationScheduler.scheduleNotification(
            context = context,
            title = "Hora de se hidratar! 💧",
            message = "Lembre-se de beber água para atingir sua meta diária.",
            triggerAtMillis = triggerTime,
            notificationId = WATER_REMINDER_NOTIFICATION_ID
        )
    }

    // Função para cancelar o lembrete de água
    private fun cancelWaterReminder() {
        val context = getApplication<Application>().applicationContext
        NotificationScheduler.cancelNotification(context, WATER_REMINDER_NOTIFICATION_ID)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}