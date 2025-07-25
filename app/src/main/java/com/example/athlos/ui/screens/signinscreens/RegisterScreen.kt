package com.example.athlos.ui.screens.signinscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.athlos.viewmodels.RegisterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(mainNavController: NavHostController) {
    val registerNavController = rememberNavController()
    val viewModel: RegisterViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.registroSucesso) {
        if (uiState.registroSucesso) {
            mainNavController.navigate("main") {
                popUpTo("register") { inclusive = true }
                popUpTo("login") { inclusive = true }
            }
        }
    }

    NavHost(
        navController = registerNavController,
        startDestination = "register_start_logic",
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        composable("register_start_logic") {
            LaunchedEffect(Unit) {
                if (uiState.nome.isNotBlank() && uiState.email.isNotBlank()) {
                    registerNavController.navigate("register_dob_gender") {
                        popUpTo("register_start_logic") { inclusive = true }
                    }
                } else {
                    registerNavController.navigate("register_intro") {
                        popUpTo("register_start_logic") { inclusive = true }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        composable("register_intro") {
            RegisterIntroScreen(registerNavController)
        }
        composable("register_name") {
            RegisterNameScreen(registerNavController, viewModel)
        }
        composable("register_dob_gender") {
            RegisterDobGenderScreen(registerNavController, viewModel)
        }
        composable("register_weight_height") {
            RegisterWeightHeightScreen(registerNavController, viewModel)
        }
        composable("register_email_password") {
            RegisterEmailPasswordScreen(registerNavController, viewModel)
        }
        composable("register_exercise") {
            RegisterExerciseScreen(registerNavController, viewModel)
        }
        composable("register_final_success") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Finalizando seu cadastro...",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterIntroScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Olá!",
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Como você gostaria de começar?",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { navController.navigate("register_name") },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Começar Cadastro")
        }
    }
}