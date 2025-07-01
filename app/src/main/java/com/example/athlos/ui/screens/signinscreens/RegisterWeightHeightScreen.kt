package com.example.athlos.ui.screens.signinscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.athlos.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Scale
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.athlos.viewmodels.RegisterViewModel
import com.example.athlos.ui.screens.defaultTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterWeightHeightScreen(navController: NavHostController, viewModel: RegisterViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var weightInput by remember { mutableStateOf(uiState.peso) }
    LaunchedEffect(uiState.peso) { weightInput = uiState.peso }

    var heightInput by remember { mutableStateOf(uiState.altura) }
    LaunchedEffect(uiState.altura) { heightInput = uiState.altura }


    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.scale))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        speed = 1f
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(150.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Agora você pode nos informar seu peso e altura?",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Tudo isso entra nos nossos cálculos mágicos pra te colocar no shape!",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = weightInput,
            onValueChange = {
                val filtered = it.filter { char -> char.isDigit() || (char == '.' && !it.contains('.')) }
                weightInput = filtered
                viewModel.updatePeso(filtered)
            },
            label = { Text("Seu peso (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            trailingIcon = {
                if (weightInput.isNotBlank()) {
                    if (viewModel.isPesoValid()) {
                        Icon(Icons.Default.Scale, contentDescription = "Peso Válido", tint = Color.Green)
                    } else {
                        Icon(Icons.Default.Scale, contentDescription = "Peso Inválido", tint = Color.Red)
                    }
                }
            },
            colors = defaultTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = heightInput,
            onValueChange = {
                val filtered = it.filter { char -> char.isDigit() || (char == '.' && !it.contains('.')) }
                heightInput = filtered
                viewModel.updateAltura(filtered)
            },
            label = { Text("Sua altura (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            trailingIcon = {
                if (heightInput.isNotBlank()) {
                    if (viewModel.isAlturaValid()) {
                        Icon(Icons.Default.Height, contentDescription = "Altura Válida", tint = Color.Green)
                    } else {
                        Icon(Icons.Default.Height, contentDescription = "Altura Inválida", tint = Color.Red)
                    }
                }
            },
            colors = defaultTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (viewModel.isPesoValid() && viewModel.isAlturaValid()) {
                    navController.navigate("register_email_password")
                } else {
                    Toast.makeText(context, "Por favor, insira valores válidos para peso e altura.", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = viewModel.isPesoValid() && viewModel.isAlturaValid(),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Próximo")
        }
    }
}