package com.example.athlos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.athlos.R
import com.example.athlos.viewmodels.HomeViewModel
import com.example.athlos.data.model.User
import com.example.athlos.data.model.Workout
import com.example.athlos.viewmodels.MacroTargets

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = homeViewModel) {
        homeViewModel.refreshUserData()
    }

    val favoriteWorkouts = uiState.favoriteWorkouts

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text(
                text = "Olá, ${uiState.currentUserData?.nome ?: "Usuário"}!",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                        progress = (uiState.aguaAtual / uiState.aguaMeta.toFloat()).coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "🎯 Meta: ${uiState.currentUserData?.meta ?: "Não definida"}",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "🏋️ Dias de treino/semana: ${uiState.diasTreino}",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            NutritionalInfoCard(
                tdee = uiState.tdee,
                caloriesForLoss = uiState.caloriesForLoss,
                caloriesForGain = uiState.caloriesForGain,
                macrosForLoss = uiState.macrosForLoss,
                macrosForGain = uiState.macrosForGain
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Seus treinos favoritos",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (favoriteWorkouts.isEmpty()) {
                Text(
                    text = "Você ainda não favoritou nenhum treino. Vá para a tela de Treinos para explorar!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            } else {
                LazyColumn {
                    items(favoriteWorkouts) { workout ->
                        WorkoutCardDisplay(workout = workout)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutCardDisplay(workout: Workout) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = workout.imageRes),
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.title,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = workout.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
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
    if (tdee <= 0) return // Não mostra o card se os cálculos não foram feitos

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 Minhas Metas Nutricionais 🥗",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Calorias para manter o peso: ${tdee.toInt()} kcal/dia",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Seção para Perder Peso
            Text(
                text = "Para Perder Peso (Déficit)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Meta: ${caloriesForLoss.toInt()} kcal/dia")
            Text("Macros: P: ${macrosForLoss.proteinGrams}g, G: ${macrosForLoss.fatGrams}g, C: ${macrosForLoss.carbGrams}g")

            Spacer(modifier = Modifier.height(16.dp))

            // Seção para Ganhar Peso
            Text(
                text = "Para Ganhar Peso (Superávit)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Meta: ${caloriesForGain.toInt()} kcal/dia")
            Text("Macros: P: ${macrosForGain.proteinGrams}g, G: ${macrosForGain.fatGrams}g, C: ${macrosForGain.carbGrams}g")
        }
    }
}