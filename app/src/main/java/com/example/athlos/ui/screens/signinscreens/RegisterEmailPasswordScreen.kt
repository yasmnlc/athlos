package com.example.athlos.ui.screens.signinscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.athlos.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import android.widget.Toast
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
import com.example.athlos.viewmodels.RegisterViewModel
import com.example.athlos.ui.screens.defaultTextFieldColors


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterEmailPasswordScreen(navController: NavHostController, viewModel: RegisterViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var emailInput by remember { mutableStateOf(uiState.email) }
    LaunchedEffect(uiState.email) { emailInput = uiState.email }

    var passwordInput by remember { mutableStateOf(uiState.senha) }
    LaunchedEffect(uiState.senha) { passwordInput = uiState.senha }

    var confirmPasswordInput by remember { mutableStateOf("") }


    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }

    val passwordsMatch = uiState.senha == confirmPasswordInput && uiState.senha.isNotBlank()


    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.email))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        speed = 1f
    )

    LaunchedEffect(uiState.registroSucesso) {
        if (uiState.registroSucesso) {
            navController.navigate("register_final_success")
        }
    }

    LaunchedEffect(uiState.erroMensagem) {
        uiState.erroMensagem?.let {
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
            modifier = Modifier.size(150.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Para finalizar, seu email e senha",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "E-mail para login e recuperação de conta, senha para sua segurança!",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = emailInput,
            onValueChange = {
                emailInput = it
                viewModel.updateEmail(it)
            },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            trailingIcon = {
                if (emailInput.isNotBlank()) {
                    Icon(
                        imageVector = if (viewModel.isEmailValid()) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "Email Validação",
                        tint = if (viewModel.isEmailValid()) Color.Green else Color.Red
                    )
                }
            },
            colors = defaultTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = passwordInput,
            onValueChange = {
                passwordInput = it
                viewModel.updateSenha(it)
            },
            label = { Text("Senha") },
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            trailingIcon = {
                val image = if (passwordVisibility)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff
                val description = if (passwordVisibility) "Ocultar senha" else "Mostrar senha"

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (passwordInput.isNotBlank()) {
                        Icon(
                            imageVector = if (viewModel.isPasswordValid()) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = "Senha Validação",
                            tint = if (viewModel.isPasswordValid()) Color.Green else Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            },
            colors = defaultTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPasswordInput,
            onValueChange = {
                confirmPasswordInput = it
            },
            label = { Text("Confirme a Senha") },
            visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            trailingIcon = {
                val image = if (confirmPasswordVisibility)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff
                val description = if (confirmPasswordVisibility) "Ocultar senha" else "Mostrar senha"

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (confirmPasswordInput.isNotBlank()) {
                        Icon(
                            imageVector = if (passwordsMatch) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = "Confirmação Senha Validação",
                            tint = if (passwordsMatch) Color.Green else Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            },
            colors = defaultTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (viewModel.isEmailValid() && viewModel.isPasswordValid() && passwordsMatch) {
                    navController.navigate("register_exercise")
                } else {
                    val message = when {
                        !viewModel.isEmailValid() -> "Por favor, insira um email válido."
                        !viewModel.isPasswordValid() -> "A senha deve ter pelo menos 6 caracteres."
                        !passwordsMatch -> "As senhas não coincidem."
                        else -> "Preencha todos os campos corretamente."
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            },
            enabled = viewModel.isEmailValid() && viewModel.isPasswordValid() && passwordsMatch,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Próximo")
        }
    }
}