package com.example.athlos.ui.screens

import android.app.Activity
import android.app.Application
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.athlos.viewmodels.LoginViewModel 
import com.example.athlos.ui.screens.defaultTextFieldColors
import com.example.athlos.viewmodels.LoginUiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.example.athlos.R
import com.example.athlos.viewmodels.GoogleLoginNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    loginViewModel: LoginViewModel = viewModel()
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Launcher para o resultado do login do Google
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                loginViewModel.handleGoogleSignInResult(account, null)
            } catch (e: ApiException) {
                loginViewModel.handleGoogleSignInResult(null, e)
            }
        } else {
            // Login cancelado pelo usuário ou falhou sem exceção
            loginViewModel.handleGoogleSignInResult(null, null)
        }
    }

    LaunchedEffect(uiState.googleNavDestination) {
        when (uiState.googleNavDestination) {
            GoogleLoginNavigation.TO_MAIN -> {
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
                loginViewModel.onNavigationComplete()
            }
            GoogleLoginNavigation.TO_REGISTRATION -> {
                navController.navigate("register") {
                    popUpTo("login") { inclusive = true }
                }
                loginViewModel.onNavigationComplete()
            }
            GoogleLoginNavigation.NONE -> {
                // Não faz nada
            }
        }
    }

    // Observa mensagens de erro
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            loginViewModel.clearMessages() // Limpa a mensagem após exibir
        }
    }

    // Observa mensagens de sucesso
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            loginViewModel.clearMessages() // Limpa a mensagem após exibir
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Bem-vindo de volta!",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { loginViewModel.updateEmail(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            colors = defaultTextFieldColors(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { loginViewModel.updatePassword(it) },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = defaultTextFieldColors(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        uiState.errorMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { loginViewModel.loginUser() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Entrar")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Botão de Login com Google
        Button(
            onClick = { googleSignInLauncher.launch(loginViewModel.getGoogleSignInIntent()) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White, // Botão branco para Google
                contentColor = Color.DarkGray // Texto cinza para Google
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_logo), // Certifique-se de ter 'google_logo.png' em res/drawable
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Entrar com Google", fontSize = 16.sp)
            }
        }


        Spacer(Modifier.height(16.dp))

        // Seção para Esqueci a senha e Cadastre-se
        TextButton(onClick = { navController.navigate("forgot_password") }) { // Adicione rota para esqueci a senha
            Text("Esqueceu a senha?", color = MaterialTheme.colorScheme.primary)
        }


        TextButton(
            onClick = { navController.navigate("register") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Não tem uma conta? Cadastre-se aqui.")
        }
    }
}