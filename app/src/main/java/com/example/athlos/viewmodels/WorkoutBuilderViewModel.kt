package com.example.athlos.viewmodels

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.api.ExerciseDbService
import com.example.athlos.data.repository.AuthRepository
import com.example.athlos.data.repository.FirebaseAuthRepository
import com.example.athlos.ui.models.CustomWorkout
import com.example.athlos.ui.models.Exercise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutBuilderState(
    val isLoading: Boolean = true,
    val exercisesByBodyPart: Map<String, List<Exercise>> = emptyMap(),
    val selectedExerciseIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val saveStatusMessage: String? = null
)

class WorkoutBuilderViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
) : ViewModel() {

    private val apiKey = "69a1f86bdfmsh0a6e1a654dae000p197607jsn6acb47efc61e"

    private val _uiState = MutableStateFlow(WorkoutBuilderState())
    val uiState: StateFlow<WorkoutBuilderState> = _uiState.asStateFlow()

    init {
        fetchAllExercises()
    }

    private fun fetchAllExercises() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val allExercises = ExerciseDbService.api.getAllExercises(apiKey = apiKey)
                val groupedExercises = allExercises
                    .filter { it.bodyPart != null }
                    .groupBy { it.bodyPart!! }

                _uiState.update {
                    it.copy(isLoading = false, exercisesByBodyPart = groupedExercises)
                }
            } catch (e: Exception) {
                Log.e("WorkoutBuilderVM", "Erro ao buscar todos os exercícios", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Falha ao carregar exercícios.") }
            }
        }
    }

    fun toggleExerciseSelection(exerciseId: String) {
        val currentSelected = _uiState.value.selectedExerciseIds.toMutableSet()
        if (currentSelected.contains(exerciseId)) {
            currentSelected.remove(exerciseId)
        } else {
            currentSelected.add(exerciseId)
        }
        _uiState.update { it.copy(selectedExerciseIds = currentSelected) }
    }

    fun saveWorkout(workoutName: String) {
        if (workoutName.isBlank() || _uiState.value.selectedExerciseIds.isEmpty()) {
            _uiState.update { it.copy(saveStatusMessage = "Dê um nome ao treino e selecione pelo menos um exercício.") }
            return
        }

        viewModelScope.launch {
            try {
                val newWorkout = CustomWorkout(
                    name = workoutName,
                    exerciseIds = _uiState.value.selectedExerciseIds.toList()
                )
                authRepository.saveCustomWorkout(newWorkout)
                _uiState.update {
                    it.copy(
                        saveStatusMessage = "Treino '$workoutName' salvo com sucesso!",
                        selectedExerciseIds = emptySet() // Limpa a seleção após salvar
                    )
                }
            } catch (e: Exception) {
                Log.e("WorkoutBuilderVM", "Erro ao salvar treino", e)
                _uiState.update { it.copy(saveStatusMessage = "Erro ao salvar o treino.") }
            }
        }
    }

    fun clearSaveStatusMessage() {
        _uiState.update { it.copy(saveStatusMessage = null) }
    }
}