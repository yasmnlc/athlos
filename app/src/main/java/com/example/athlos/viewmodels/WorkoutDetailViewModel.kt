package com.example.athlos.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athlos.api.ExerciseDbService
import com.example.athlos.api.YouTubeService
import com.example.athlos.data.repository.AuthRepository
import com.example.athlos.data.repository.FirebaseAuthRepository
import com.example.athlos.data.repository.ExerciseRepository
import com.example.athlos.data.repository.FirestoreExerciseRepository
import com.example.athlos.ui.models.CustomWorkout
import com.example.athlos.ui.models.Exercise
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

data class WorkoutDetailState(
    val isLoading: Boolean = true,
    val workout: CustomWorkout? = null,
    val exercises: List<Exercise> = emptyList(),
    val errorMessage: String? = null
)

class WorkoutDetailViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance()),
    private val exerciseRepository: ExerciseRepository = FirestoreExerciseRepository(FirebaseFirestore.getInstance())
) : ViewModel() {

    private val youTubeApiKey = "AIzaSyDF71ScCOEuw-sm9qAlAIJ7vF-X79mt978"
    private val exerciseDbApiKey = "69a1f86bdfmsh0a6e1a654dae000p197607jsn6acb47efc61e"

    private val _uiState = MutableStateFlow(WorkoutDetailState())
    val uiState: StateFlow<WorkoutDetailState> = _uiState.asStateFlow()

    fun loadWorkoutDetails(workoutId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val customWorkout = authRepository.getCustomWorkouts().find { it.id == workoutId }

                if (customWorkout == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Treino não encontrado.") }
                    return@launch
                }

                val exerciseIds = customWorkout.exerciseIds.orEmpty()

                val exerciseDetails = coroutineScope {
                    exerciseIds.map { exerciseId ->
                        async {
                            // 1. Tenta buscar do Firestore (cache) primeiro
                            var exercise = exerciseRepository.getExerciseById(exerciseId)

                            // 2. Se não estiver no cache (Cache Miss), busca na API
                            if (exercise == null) {
                                Log.d("WorkoutDetailVM", "Cache MISS para ID $exerciseId. Buscando na API...")
                                try {
                                    exercise = ExerciseDbService.api.getExerciseById(
                                        id = exerciseId,
                                        apiKey = exerciseDbApiKey
                                    )
                                    Log.d("WorkoutDetailVM", "API HIT para ID $exerciseId: ${exercise.name}")

                                    // 3. Salva o exercício recém-buscado no Firestore (cache)
                                    exerciseRepository.saveExercise(exercise)
                                    Log.d("WorkoutDetailVM", "Exercício $exerciseId salvo no cache.")

                                } catch (e: Exception) {
                                    Log.w("WorkoutDetailVM", "⚠️ Exercício ID $exerciseId não encontrado na API. Será ignorado.", e)
                                    return@async null // Retorna nulo se falhar na API
                                }
                            } else {
                                Log.d("WorkoutDetailVM", "Cache HIT para ID $exerciseId: ${exercise.name}")
                            }

                            // 4.  Busca vídeo do YouTube se não existir
                            if (exercise.videoId == null) {
                                val videoId = findYouTubeVideoId(exercise.name)
                                exercise = exercise.copy(videoId = videoId)
                                exerciseRepository.saveExercise(exercise)
                            }

                            exercise
                        }
                    }.awaitAll().filterNotNull() // Filtra qualquer exercício que não foi encontrado em lugar nenhum
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        workout = customWorkout,
                        exercises = exerciseDetails
                    )
                }

            } catch (e: Exception) {
                Log.e("WorkoutDetailViewModel", "Falha ao carregar detalhes do treino: ${e.message}", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Falha ao carregar detalhes do treino.") }
            }
        }
    }

    private suspend fun findYouTubeVideoId(exerciseName: String?): String? {
        if (exerciseName == null) return null
        return try {
            val searchQuery = "$exerciseName exercise tutorial"
            val response = YouTubeService.api.searchVideos(query = searchQuery, apiKey = youTubeApiKey)
            response.items.firstOrNull()?.id?.videoId
        } catch (e: Exception) {
            Log.e("WorkoutDetailViewModel", "Erro ao buscar vídeo do YouTube para '$exerciseName': ${e.message}", e)
            null
        }
    }
}