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
        loadWorkoutCategories()
    }

    private fun loadWorkoutCategories() {

        _uiState.update { it.copy(loading = true, errorMessage = null) }

        val workoutCategories = listOf(
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
        _uiState.update { it.copy(workouts = workoutCategories, loading = false) }
        Log.d("TrainingViewModel", "Categorias de treino carregadas.")
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
}