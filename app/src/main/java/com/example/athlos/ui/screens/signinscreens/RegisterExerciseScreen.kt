package com.example.athlos.ui.screens.signinscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.athlos.R
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import com.example.athlos.viewmodels.RegisterViewModel
import com.example.athlos.ui.screens.defaultTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterExerciseScreen(navController: NavHostController, viewModel: RegisterViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val activityLevels = listOf(
        "Sedentário (Pouco ou nenhum exercício)",
        "Levemente ativo (Exercício leve 1–3 dias/semana)",
        "Moderadamente ativo (Exercício moderado 3–5 dias/semana)",
        "Muito ativo (Exercício intenso 6–7 dias/semana)",
        "Extremamente ativo (Atleta/Exercício pesado + trabalho físico)"
    )

    var selectedLevel by remember { mutableStateOf(uiState.diasSemana) }
    LaunchedEffect(uiState.diasSemana) { selectedLevel = uiState.diasSemana }


    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.exercise))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        speed = 1f
    )

    val registrationError = uiState.erroMensagem

    LaunchedEffect(registrationError) {
        registrationError?.let {
            Toast.makeText(context, "Erro no cadastro: $it", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(180.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Qual o seu nível de atividade física?",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Isso é importante para os cálculos de calorias!",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedLevel,
                onValueChange = { },
                readOnly = true,
                label = { Text("Nível de Atividade") },
                placeholder = { Text("Selecione seu nível de atividade") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Nível de Atividade") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                activityLevels.forEach { level ->
                    DropdownMenuItem(
                        text = { Text(level, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            selectedLevel = level
                            viewModel.updatePraticaExercicios(true)
                            viewModel.updateDiasSemana(level)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- CORREÇÃO APLICADA AQUI ---
        Button(
            onClick = {
                if (viewModel.isExerciseValid()) {
                    // A única responsabilidade deste botão agora é navegar para a próxima tela.
                    navController.navigate("register_goal")
                } else {
                    Toast.makeText(context, "Por favor, selecione seu nível de atividade.", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = viewModel.isExerciseValid(),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            // Texto do botão corrigido para indicar que há um próximo passo.
            Text("Próximo")
        }
    }
}