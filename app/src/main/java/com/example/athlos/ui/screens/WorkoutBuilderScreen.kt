package com.example.athlos.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.athlos.ui.models.Exercise
import com.example.athlos.viewmodels.WorkoutBuilderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutBuilderScreen(
    viewModel: WorkoutBuilderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveStatusMessage) {
        uiState.saveStatusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSaveStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (uiState.selectedExerciseIds.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showSaveDialog = true },
                    icon = { Icon(Icons.Filled.Save, "Salvar Treino") },
                    text = { Text("Concluir Treino (${uiState.selectedExerciseIds.size})") }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
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
            else -> {
                val bodyParts = uiState.exercisesByBodyPart.keys.toList().sorted()
                var selectedTabIndex by remember { mutableStateOf(0) }

                Column(modifier = Modifier.padding(paddingValues)) {
                    ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
                        bodyParts.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(text = title.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }

                    if (bodyParts.isNotEmpty()) {
                        val selectedBodyPart = bodyParts.getOrNull(selectedTabIndex)
                        val exercisesForBodyPart = uiState.exercisesByBodyPart.getOrDefault(selectedBodyPart, emptyList())

                        LazyColumn(contentPadding = PaddingValues(16.dp)) {
                            items(exercisesForBodyPart) { exercise ->
                                SelectableExerciseItem(
                                    exercise = exercise,
                                    isSelected = uiState.selectedExerciseIds.contains(exercise.id),
                                    onToggleSelection = { viewModel.toggleExerciseSelection(exercise.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveWorkoutDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { workoutName ->
                viewModel.saveWorkout(workoutName)
                showSaveDialog = false
            }
        )
    }
}

@Composable
fun SelectableExerciseItem(
    exercise: Exercise,
    isSelected: Boolean,
    onToggleSelection: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelection() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggleSelection() }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = (exercise.name ?: "Exercício sem nome").replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveWorkoutDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var workoutName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Salvar Treino") },
        text = {
            OutlinedTextField(
                value = workoutName,
                onValueChange = { workoutName = it },
                label = { Text("Nome do Treino") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(workoutName) },
                enabled = workoutName.isNotBlank()
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}