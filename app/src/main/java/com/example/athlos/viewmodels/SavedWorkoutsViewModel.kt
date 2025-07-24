package com.example.athlos.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.data.repository.AuthRepository
import com.example.athlos.data.repository.FirebaseAuthRepository
import com.example.athlos.ui.models.CustomWorkout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SavedWorkoutsUiState(
    val isLoading: Boolean = true,
    val savedWorkouts: List<CustomWorkout> = emptyList(),
    val errorMessage: String? = null
)

class SavedWorkoutsViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedWorkoutsUiState())
    val uiState: StateFlow<SavedWorkoutsUiState> = _uiState.asStateFlow()

    init {
        loadSavedWorkouts()
    }

    fun loadSavedWorkouts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val workouts = authRepository.getCustomWorkouts()
                _uiState.update { it.copy(isLoading = false, savedWorkouts = workouts) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Falha ao carregar treinos.") }
            }
        }
    }

    fun deleteWorkout(workoutId: String) {
        viewModelScope.launch {
            try {
                authRepository.deleteCustomWorkout(workoutId)
                // Remove o treino da lista local para atualizar a UI instantaneamente
                _uiState.update {
                    val updatedList = it.savedWorkouts.filter { workout -> workout.id != workoutId }
                    it.copy(savedWorkouts = updatedList)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Falha ao apagar treino.") }
            }
        }
    }
}