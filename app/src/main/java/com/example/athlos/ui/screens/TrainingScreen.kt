package com.example.athlos.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.athlos.ui.models.Workout
import com.example.athlos.ui.components.SaveWorkoutDialog
import com.example.athlos.ui.models.Exercise
import com.example.athlos.viewmodels.TrainingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(
    trainingViewModel: TrainingViewModel = viewModel(),
    onSavedWorkoutsClick: () -> Unit,
    onNavigateToExerciseList: (String) -> Unit
) {
    val uiState by trainingViewModel.uiState.collectAsState()
    var showSaveDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveStatusMessage) {
        uiState.saveStatusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            trainingViewModel.clearSaveStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.isBuildingMode && uiState.selectedExerciseIds.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showSaveDialog = true },
                    icon = { Icon(Icons.Filled.Save, "Salvar Treino") },
                    text = { Text("Concluir Treino (${uiState.selectedExerciseIds.size})") }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { trainingViewModel.toggleBuildMode() },
                    modifier = Modifier.weight(1f),
                    colors = if (uiState.isBuildingMode) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary) else ButtonDefaults.buttonColors()
                ) {
                    Text(if (uiState.isBuildingMode) "Cancelar" else "Montar Treino")
                }
                OutlinedButton(
                    onClick = onSavedWorkoutsClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Treinos Salvos")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (uiState.isBuildingMode) "Selecione os exercícios" else "Explore por grupo muscular",
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.workouts) { workout ->
                        ExpandableWorkoutCard(
                            workout = workout,
                            isExpanded = uiState.expandedWorkoutId == workout.id,
                            isBuildingMode = uiState.isBuildingMode,
                            exercises = uiState.exercisesForSelectedWorkout,
                            isLoadingExercises = uiState.isLoadingExercises,
                            selectedExerciseIds = uiState.selectedExerciseIds,
                            onCardClick = {
                                if (uiState.isBuildingMode) {
                                    trainingViewModel.onWorkoutCardClicked(workout)
                                } else {
                                    onNavigateToExerciseList(workout.apiBodyPart)
                                }
                            },
                            onExerciseSelected = { exerciseId ->
                                trainingViewModel.toggleExerciseSelection(exerciseId)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveWorkoutDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { workoutName ->
                trainingViewModel.saveWorkout(workoutName)
                showSaveDialog = false
            }
        )
    }
}

@Composable
fun ExpandableWorkoutCard(
    workout: Workout,
    isExpanded: Boolean,
    isBuildingMode: Boolean,
    exercises: List<Exercise>,
    isLoadingExercises: Boolean,
    selectedExerciseIds: Set<String>,
    onCardClick: () -> Unit,
    onExerciseSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(painter = painterResource(id = workout.imageRes), contentDescription = null, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = workout.title, style = MaterialTheme.typography.titleLarge)
                    Text(text = workout.description, style = MaterialTheme.typography.bodyMedium)
                }
            }

            AnimatedVisibility(visible = isExpanded && isBuildingMode) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    if (isLoadingExercises) {
                        Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center){
                            CircularProgressIndicator()
                        }
                    } else {
                        exercises.forEach { exercise ->
                            SelectableExerciseRow(
                                exercise = exercise,
                                isSelected = selectedExerciseIds.contains(exercise.id),
                                onExerciseSelected = { onExerciseSelected(exercise.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableExerciseRow(
    exercise: Exercise,
    isSelected: Boolean,
    onExerciseSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExerciseSelected() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text((exercise.name ?: "Exercício").replaceFirstChar { it.uppercase() })
        }
        Checkbox(checked = isSelected, onCheckedChange = { onExerciseSelected() })
    }
}