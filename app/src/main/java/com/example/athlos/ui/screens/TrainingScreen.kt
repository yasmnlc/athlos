package com.example.athlos.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.athlos.data.model.Workout
import com.example.athlos.viewmodels.TrainingViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(
    trainingViewModel: TrainingViewModel = viewModel()
) {
    val uiState by trainingViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = trainingViewModel) {
        trainingViewModel.refreshWorkouts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todos os Treinos", color = MaterialTheme.colorScheme.onPrimaryContainer) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (uiState.loading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(
                    text = "Erro: ${uiState.errorMessage}",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 18.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Explore e favorite seus treinos",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn {
                    items(uiState.workouts) { workout ->
                        WorkoutCardWithFavorite(
                            workout = workout,
                            onToggleFavorite = { id, isFav -> trainingViewModel.toggleFavorite(id, isFav) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutCardWithFavorite(workout: Workout, onToggleFavorite: (String, Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Image(
                    painter = painterResource(id = workout.imageRes),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = workout.title,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = workout.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            val favoriteIcon = if (workout.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder
            val tintColor = if (workout.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

            val scale by animateFloatAsState(
                targetValue = if (workout.isFavorite) 1.2f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "favoriteIconScale"
            )

            IconButton(
                onClick = { onToggleFavorite(workout.id, workout.isFavorite) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = favoriteIcon,
                    contentDescription = "Marcar como favorito",
                    tint = tintColor,
                    modifier = Modifier.scale(scale)
                )
            }
        }
    }
}