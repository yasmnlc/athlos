package com.example.athlos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.athlos.ui.models.CustomWorkout
import com.example.athlos.viewmodels.HomeViewModel
import com.example.athlos.viewmodels.MacroTargets

@Composable
fun HomeScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        homeViewModel.refreshUserData()
    }

    if (uiState.loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Erro: ${uiState.errorMessage}",
                color = MaterialTheme.colorScheme.error,
                fontSize = 18.sp
            )
        }
    } else {
        // A tela inteira é uma LazyColumn para ser rolável e evitar erros.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Cada parte da tela é um "item"
            item {
                Text(
                    text = "Olá, ${uiState.currentUserData?.nome ?: "Usuário"}!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                DailySummaryCard(uiState = uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                NutritionalInfoCard(
                    tdee = uiState.tdee,
                    caloriesForLoss = uiState.caloriesForLoss,
                    caloriesForGain = uiState.caloriesForGain,
                    macrosForLoss = uiState.macrosForLoss,
                    macrosForGain = uiState.macrosForGain
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "Seus treinos salvos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            //Lista de treinos
            if (uiState.savedWorkouts.isEmpty()) {
                item {
                    Text(
                        text = "Você ainda não salvou nenhum treino. Vá para a tela de Treinos para criar o seu!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                }
            } else {
                items(uiState.savedWorkouts) { workout ->
                    SavedWorkoutCard(
                        workout = workout,
                        onClick = {
                            navController.navigate("savedWorkoutDetail/${workout.id}")
                        }
                    )
                }
            }
        }
    }
}

// --- COMPOSABLES AUXILIARES ---

@Composable
fun DailySummaryCard(uiState: com.example.athlos.viewmodels.HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Resumo do dia",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "💧 Água: ${uiState.aguaAtual} ml de ${uiState.aguaMeta} ml",
                color = MaterialTheme.colorScheme.onSurface
            )
            LinearProgressIndicator(
                progress = { (uiState.aguaAtual / uiState.aguaMeta.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🎯 Meta: ${uiState.currentUserData?.goal ?: "Não definida"}",
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "🏋️ Nível de atividade: ${uiState.currentUserData?.diasSemana ?: "N/A"}",
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun NutritionalInfoCard(
    tdee: Double,
    caloriesForLoss: Double,
    caloriesForGain: Double,
    macrosForLoss: MacroTargets,
    macrosForGain: MacroTargets
) {
    if (tdee <= 0) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Suas Metas Nutricionais 📊🥗",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Calorias para manter o peso: ${tdee.toInt()} kcal/dia",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Para Perder Peso (Déficit)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Macros: P: ${macrosForLoss.proteinGrams}g, G: ${macrosForLoss.fatGrams}g, C: ${macrosForLoss.carbGrams}g | Meta: ${caloriesForLoss.toInt()} kcal/dia")
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Para Ganhar Peso (Superávit)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Macros: P: ${macrosForGain.proteinGrams}g, G: ${macrosForGain.fatGrams}g, C: ${macrosForGain.carbGrams}g | Meta: ${caloriesForGain.toInt()} kcal/dia")
        }
    }
}

@Composable
fun SavedWorkoutCard(workout: CustomWorkout, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = "Ícone de Treino",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${workout.exerciseIds.size} exercícios",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}