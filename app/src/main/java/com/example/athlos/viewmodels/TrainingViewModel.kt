package com.example.athlos.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.R
import com.example.athlos.ui.models.Workout
import com.example.athlos.data.repository.AuthRepository
import com.example.athlos.data.repository.FirebaseAuthRepository
import com.example.athlos.ui.models.CustomWorkout
import com.example.athlos.ui.models.Exercise
import com.example.athlos.api.ExerciseDbService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrainingUiState(
    val workouts: List<Workout> = emptyList(),
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val isBuildingMode: Boolean = false,
    val expandedWorkoutId: String? = null,
    val exercisesForSelectedWorkout: List<Exercise> = emptyList(),
    val isLoadingExercises: Boolean = false,
    val selectedExerciseIds: Set<String> = emptySet(),
    val saveStatusMessage: String? = null,
    val isSavingFavorite: Boolean = false
)

class TrainingViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
) : ViewModel() {

    private val apiKey = "69a1f86bdfmsh0a6e1a654dae000p197607jsn6acb47efc61e"

    private val _uiState = MutableStateFlow(TrainingUiState())
    val uiState: StateFlow<TrainingUiState> = _uiState.asStateFlow()

    init {
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

                    val allMockWorkouts = listOf(
                        Workout("treino_peito", "Treino de Peito", "Desenvolvimento, Supino, Crucifixo", R.drawable.chest, "chest"),
                        Workout("treino_costas", "Treino de Costas", "Puxada, Remada, Levantamento Terra", R.drawable.back, "back"),
                        Workout("treino_quadriceps", "Treino de Quadríceps", "Agachamento, Leg Press, Extensora", R.drawable.quads, "upper legs"),
                        Workout("treino_ombros", "Treino de Ombros", "Desenvolvimento, Elevação Lateral, Remada Alta", R.drawable.shoulder, "shoulders"),
                        Workout("treino_biceps", "Treino de Bíceps", "Rosca Direta, Rosca Alternada, Rosca Concentrada", R.drawable.biceps, "upper arms"),
                        Workout("treino_triceps", "Treino de Tríceps", "Extensão, Tríceps Testa, Mergulho", R.drawable.triceps, "upper arms"),
                        Workout("treino_abdomen", "Treino de Abdômen", "Abdominal, Prancha, Elevação de Pernas", R.drawable.abs, "waist"),
                        Workout("treino_gluteos", "Treino de Glúteos", "Agachamento, Glúteo Máquina, Elevação Pélvica", R.drawable.glutes, "upper legs"),
                        Workout("treino_dorsal", "Treino de Dorsal", "Puxada, Remada, Pullover", R.drawable.dorsal, "back"),
                        Workout("treino_posterior", "Treino de Posterior", "Stiff, Flexora, Bom Dia", R.drawable.hamstrings, "upper legs"),
                        Workout("treino_obliquos", "Treino de Oblíquos", "Rotação de Tronco, Flexão Lateral", R.drawable.obliquo, "waist"),
                        Workout("treino_trapezio", "Treino de Trapézio", "Remada Alta, Encolhimento", R.drawable.trapezius, "neck"),
                        Workout("treino_panturrilha", "Treino de Panturrilha", "Elevação em Pé, Elevação Sentado", R.drawable.calfs, "lower legs"),
                        Workout("treino_antebraco", "Treino de Antebraço", "Rosca Punho, Flexão Inversa", R.drawable.forearm, "lower arms")
                    )

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

    fun refreshWorkouts() {
        loadAllWorkoutsAndFavorites()
    }

    fun toggleBuildMode() {
        val isBuilding = !_uiState.value.isBuildingMode
        _uiState.update {
            it.copy(
                isBuildingMode = isBuilding,
                selectedExerciseIds = if (!isBuilding) emptySet() else it.selectedExerciseIds,
                expandedWorkoutId = null,
                exercisesForSelectedWorkout = emptyList()
            )
        }
    }

    fun onWorkoutCardClicked(workout: Workout) {
        val currentlyExpandedId = _uiState.value.expandedWorkoutId
        if (currentlyExpandedId == workout.id) {
            _uiState.update { it.copy(expandedWorkoutId = null, exercisesForSelectedWorkout = emptyList()) }
        } else {
            _uiState.update { it.copy(expandedWorkoutId = workout.id, isLoadingExercises = true, exercisesForSelectedWorkout = emptyList()) }
            viewModelScope.launch {
                try {
                    val exercises = ExerciseDbService.api.getExercisesByBodyPart(
                        bodyPart = workout.apiBodyPart,
                        apiKey = apiKey,
                        apiHost = "exercisedb.p.rapidapi.com"
                    )
                    _uiState.update { it.copy(exercisesForSelectedWorkout = exercises, isLoadingExercises = false) }
                } catch (e: Exception) {
                    Log.e("TrainingViewModel", "Erro ao buscar exercícios para ${workout.apiBodyPart}", e)
                    _uiState.update { it.copy(errorMessage = "Erro ao buscar exercícios.", isLoadingExercises = false) }
                }
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
            _uiState.update { it.copy(saveStatusMessage = "Dê um nome e selecione pelo menos um exercício.") }
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
                        selectedExerciseIds = emptySet(),
                        isBuildingMode = false,
                        expandedWorkoutId = null,
                        exercisesForSelectedWorkout = emptyList()
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveStatusMessage = "Erro ao salvar o treino.") }
            }
        }
    }

    fun clearSaveStatusMessage() {
        _uiState.update { it.copy(saveStatusMessage = null) }
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