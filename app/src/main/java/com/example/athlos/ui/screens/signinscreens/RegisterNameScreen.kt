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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.athlos.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.text.input.KeyboardType
import com.example.athlos.viewmodels.RegisterViewModel
import com.example.athlos.ui.screens.defaultTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterNameScreen(navController: NavHostController, viewModel: RegisterViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var nameInput by remember { mutableStateOf(uiState.nome) }
    LaunchedEffect(uiState.nome) { nameInput = uiState.nome }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.smileyface))
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
            text = "Qual o seu nome?",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Como você gostaria de ser chamado?",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = nameInput,
            onValueChange = {
                nameInput = it
                viewModel.updateNome(it)
            },
            label = { Text("Seu nome") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            trailingIcon = {
                if (nameInput.isNotBlank()) {
                    Icon(
                        imageVector = if (viewModel.isNameValid()) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "Validação Nome",
                        tint = if (viewModel.isNameValid()) Color.Green else Color.Red
                    )
                }
            },
            colors = defaultTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (viewModel.isNameValid()) {
                    navController.navigate("register_dob_gender")
                } else { }
            },
            enabled = viewModel.isNameValid(),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Próximo")
        }
    }
}