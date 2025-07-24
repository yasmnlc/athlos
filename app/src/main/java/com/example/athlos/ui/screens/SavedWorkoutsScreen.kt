package com.example.athlos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.athlos.ui.models.CustomWorkout
import com.example.athlos.viewmodels.SavedWorkoutsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedWorkoutsScreen(
    onWorkoutClick: (String) -> Unit,
    viewModel: SavedWorkoutsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meus Treinos Salvos") })
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
            uiState.savedWorkouts.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Você ainda não salvou nenhum treino.")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.savedWorkouts) { workout ->
                        SavedWorkoutCard(
                            workout = workout,
                            onDeleteClick = { viewModel.deleteWorkout(workout.id) },
                            onEditClick = { /* Lógica de edição futura */ },
                            onCardClick = { onWorkoutClick(workout.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedWorkoutCard(
    workout: CustomWorkout,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onCardClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = workout.bodyParts.joinToString(", ").replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Editar Treino")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Apagar Treino")
            }
        }
    }
}