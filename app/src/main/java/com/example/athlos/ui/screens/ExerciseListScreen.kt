package com.example.athlos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.athlos.ui.models.Exercise
import com.example.athlos.viewmodels.ExerciseViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(
    bodyPart: String,
    viewModel: ExerciseViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = bodyPart) {
        viewModel.getExercises(bodyPart)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Exercícios para ${bodyPart.replaceFirstChar { it.uppercase() }}") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.exerciseList) { exercise ->
                            ExerciseDetailCard(exercise = exercise)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseDetailCard(exercise: Exercise) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = (exercise.name ?: "Exercício sem nome").replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (exercise.videoId != null) {
                YoutubePlayer(
                    youtubeVideoId = exercise.videoId!!,
                    lifecycleOwner = LocalLifecycleOwner.current
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Gray.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Vídeo não disponível", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Músculo Alvo: ${(exercise.target ?: "N/A").replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Equipamento: ${(exercise.equipment ?: "N/A").replaceFirstChar { it.uppercase() }}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun YoutubePlayer(
    youtubeVideoId: String,
    lifecycleOwner: LifecycleOwner
) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        factory = { context ->
            YouTubePlayerView(context).apply {
                lifecycleOwner.lifecycle.addObserver(this)
                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.cueVideo(youtubeVideoId, 0f)
                    }
                })
            }
        }
    )
}