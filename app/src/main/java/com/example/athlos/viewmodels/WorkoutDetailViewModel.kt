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

                // Log 1: Verifique os IDs de exercício do treino personalizado
                Log.d("WorkoutDetailViewModel", "IDs de exercícios do treino: ${customWorkout?.exerciseIds}")

                if (customWorkout == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Treino não encontrado.") }
                    return@launch
                }

                val exerciseIds = customWorkout.exerciseIds.orEmpty().toSet()
                val invalidExerciseIds = mutableListOf<String>()

                // === CORREÇÃO 1: API ExerciseDbService ===
                // Removemos o .body() pois ExerciseDbApi.getAllExercises já retorna List<Exercise> diretamente.
                // Adicionamos 'apiHost' conforme a definição em ExerciseDbApi.kt.
                val allExercisesList = ExerciseDbService.api.getAllExercises(
                    apiKey = exerciseDbApiKey,
                    apiHost = "exercisedb.p.rapidapi.com", //
                    limit = 1500
                )
                val validExerciseMap = allExercisesList.associateBy { it.id }

                // Log 2: Verifique quantos exercícios foram obtidos da API
                Log.d("WorkoutDetailViewModel", "Total de exercícios obtidos da API: ${validExerciseMap.size}")


                val exerciseDetails = coroutineScope {
                    exerciseIds.map { exerciseId ->
                        async {
                            var exercise = exerciseRepository.getExerciseById(exerciseId)

                            if (exercise == null) {
                                // Log 3: Exercício não encontrado no Firestore
                                Log.d("WorkoutDetailViewModel", "Exercício com ID $exerciseId NÃO encontrado no Firestore. Tentando API...")
                                exercise = validExerciseMap[exerciseId]

                                if (exercise == null) {
                                    Log.w("WorkoutDetailVM", "⚠️ Exercício ID $exerciseId não encontrado na API. Será ignorado.")
                                    invalidExerciseIds.add(exerciseId)
                                    return@async null
                                }

                                exerciseRepository.saveExercise(exercise)
                                // Log 4: Exercício encontrado na API
                                Log.d("WorkoutDetailViewModel", "Exercício com ID $exerciseId encontrado na API: ${exercise.name}")
                            } else {
                                // Log 5: Exercício encontrado no Firestore
                                Log.d("WorkoutDetailViewModel", "Exercício com ID $exerciseId encontrado no Firestore: ${exercise.name}")
                            }

                            // === CORREÇÃO 2: Campo do ID do YouTube ===
                            // Revertemos para 'videoId' pois a imagem mostrou que 'youtubeVideoId' é uma referência não resolvida.
                            if (exercise.videoId == null) { //
                                val videoId = findYouTubeVideoId(exercise.name)
                                exercise = exercise.copy(videoId = videoId) //
                                exerciseRepository.saveExercise(exercise)
                            }

                            exercise
                        }
                    }.awaitAll().filterNotNull()
                }

                if (invalidExerciseIds.isNotEmpty()) {
                    Log.w("WorkoutDetailVM", "⚠️ ${invalidExerciseIds.size} exercício(s) inválido(s): $invalidExerciseIds")
                }

                // Log 6: Verifique a lista final de exercícios antes de atualizar o UI State
                Log.d("WorkoutDetailViewModel", "Total de exercícios prontos para exibição: ${exerciseDetails.size}")
                Log.d("WorkoutDetailViewModel", "Nomes dos exercícios para exibição: ${exerciseDetails.map { it.name }}")


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