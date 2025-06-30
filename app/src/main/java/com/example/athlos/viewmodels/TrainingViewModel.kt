package com.example.athlos.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.R
import com.example.athlos.data.model.Workout
import com.example.athlos.data.repository.AuthRepository
import com.example.athlos.data.repository.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrainingUiState(
    val workouts: List<Workout> = emptyList(),
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val isSavingFavorite: Boolean = false
)

class TrainingViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrainingUiState())
    val uiState: StateFlow<TrainingUiState> = _uiState.asStateFlow()

    init {
        loadAllWorkoutsAndFavorites()
    }

    fun refreshWorkouts() {
        loadAllWorkoutsAndFavorites()
    }

    private fun loadAllWorkoutsAndFavorites() {
        _uiState.value = _uiState.value.copy(loading = true, errorMessage = null)
        viewModelScope.launch {
            val user = authRepository.currentUser
            if (user != null) {
                try {
                    val userData = authRepository.getUserData(user.uid)
                    val favoriteWorkoutIds = userData?.favoriteWorkouts ?: emptyList()

                    // Lista de todos os treinos disponíveis (adaptada da sua TrainingScreen atual)
                    val allMockWorkouts = listOf(
                        Workout("treino_peito", "Treino de Peito", "Desenvolvimento, Supino, Crucifixo", R.drawable.chest, false),
                        Workout("treino_costas", "Treino de Costas", "Puxada, Remada, Levantamento Terra", R.drawable.back, false),
                        Workout("treino_quadriceps", "Treino de Quadríceps", "Agachamento, Leg Press, Extensora", R.drawable.quads, false),
                        Workout("treino_ombros", "Treino de Ombros", "Desenvolvimento, Elevação Lateral, Remada Alta", R.drawable.shoulder, false),
                        Workout("treino_biceps", "Treino de Bíceps", "Rosca Direta, Rosca Alternada, Rosca Concentrada", R.drawable.biceps, false),
                        Workout("treino_triceps", "Treino de Tríceps", "Extensão, Tríceps Testa, Mergulho", R.drawable.triceps, false),
                        Workout("treino_abdomen", "Treino de Abdômen", "Abdominal, Prancha, Elevação de Pernas", R.drawable.abs, false),
                        Workout("treino_gluteos", "Treino de Glúteos", "Agachamento, Glúteo Máquina, Elevação Pélvica", R.drawable.glutes, false),
                        Workout("treino_dorsal", "Treino de Dorsal", "Puxada, Remada, Pullover", R.drawable.dorsal, false),
                        Workout("treino_posterior", "Treino de Posterior", "Stiff, Flexora, Bom Dia", R.drawable.hamstrings, false),
                        Workout("treino_obliquos", "Treino de Oblíquos", "Rotação de Tronco, Flexão Lateral", R.drawable.obliquo, false),
                        Workout("treino_trapezio", "Treino de Trapézio", "Remada Alta, Encolhimento", R.drawable.trapezius, false),
                        Workout("treino_panturrilha", "Treino de Panturrilha", "Elevação em Pé, Elevação Sentado", R.drawable.calfs, false),
                        Workout("treino_antebraco", "Treino de Antebraço", "Rosca Punho, Flexão Inversa", R.drawable.forearm, false)
                    )

                    // Atualiza o status de favorito de cada treino na lista completa
                    val updatedWorkouts = allMockWorkouts.map { workout ->
                        workout.copy(isFavorite = favoriteWorkoutIds.contains(workout.id))
                    }

                    _uiState.value = _uiState.value.copy(
                        workouts = updatedWorkouts,
                        loading = false
                    )
                    Log.d("TrainingViewModel", "Todos os treinos carregados. Favoritos do usuário: $favoriteWorkoutIds")
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Erro ao carregar treinos: ${e.message}",
                        loading = false
                    )
                    Log.e("TrainingViewModel", "Erro ao carregar treinos: ${e.message}", e)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Nenhum usuário logado para carregar treinos.",
                    loading = false
                )
                Log.d("TrainingViewModel", "Nenhum usuário logado na TrainingScreen.")
            }
        }
    }

    fun toggleFavorite(workoutId: String, isCurrentlyFavorite: Boolean) {
        _uiState.value = _uiState.value.copy(isSavingFavorite = true)
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                try {
                    val userData = authRepository.getUserData(currentUser.uid)
                    val currentFavoriteIds = userData?.favoriteWorkouts ?: emptyList()

                    val newFavoriteIds = if (isCurrentlyFavorite) {
                        currentFavoriteIds - workoutId
                    } else {
                        currentFavoriteIds + workoutId
                    }

                    authRepository.updateUserData(currentUser.uid, mapOf("favoriteWorkouts" to newFavoriteIds))

                    val updatedWorkouts = _uiState.value.workouts.map { workout ->
                        if (workout.id == workoutId) {
                            workout.copy(isFavorite = !isCurrentlyFavorite)
                        } else {
                            workout
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        workouts = updatedWorkouts,
                        isSavingFavorite = false
                    )
                    Log.d("TrainingViewModel", "Status de favorito atualizado para $workoutId: ${!isCurrentlyFavorite}")

                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Falha ao atualizar favorito: ${e.message}",
                        isSavingFavorite = false
                    )
                    Log.e("TrainingViewModel", "Erro ao atualizar favorito: ${e.message}", e)
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Nenhum usuário logado para favoritar.",
                    isSavingFavorite = false
                )
            }
        }
    }
}