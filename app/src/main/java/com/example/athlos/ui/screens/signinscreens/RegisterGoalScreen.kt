package com.example.athlos.ui.screens.signinscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.athlos.R
import com.example.athlos.viewmodels.RegisterViewModel

@Composable
fun RegisterGoalScreen(navController: NavHostController, viewModel: RegisterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedGoal by remember { mutableStateOf(uiState.goal) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.TrackChanges,
            contentDescription = "Ícone de Meta",
            modifier = Modifier.size(150.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Qual é o seu principal objetivo?",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        GoalOption(
            title = "Perder Peso",
            description = "Foco em déficit calórico para reduzir gordura corporal.",
            isSelected = selectedGoal == "Perder Peso",
            onClick = { selectedGoal = "Perder Peso" }
        )
        Spacer(Modifier.height(16.dp))
        GoalOption(
            title = "Manter Peso",
            description = "Equilibrar calorias para manter sua forma atual.",
            isSelected = selectedGoal == "Manter Peso",
            onClick = { selectedGoal = "Manter Peso" }
        )
        Spacer(Modifier.height(16.dp))
        GoalOption(
            title = "Ganhar Peso",
            description = "Foco em superávit calórico para ganho de massa muscular.",
            isSelected = selectedGoal == "Ganhar Peso",
            onClick = { selectedGoal = "Ganhar Peso" }
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.updateGoal(selectedGoal)
                val isGoogleUser = viewModel.isCurrentUserGoogleUser()
                if (isGoogleUser) {
                    viewModel.salvarDadosAdicionaisDoPerfil()
                } else {
                    viewModel.registrarUsuarioEPerfil()
                }
                navController.navigate("register_final_success")
            },
            enabled = selectedGoal.isNotBlank() && !uiState.carregando,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            if (uiState.carregando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Finalizar Cadastro")
            }
        }
    }
}

@Composable
fun GoalOption(title: String, description: String, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}