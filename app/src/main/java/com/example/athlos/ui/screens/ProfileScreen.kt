// ProfileScreen.kt
package com.example.athlos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.athlos.data.model.User // Importa a data class User
import androidx.lifecycle.viewmodel.compose.viewModel // Para ProfileViewModel

// Importa o seu ProfileViewModel
import com.example.athlos.ui.viewmodels.ProfileViewModel
import com.example.athlos.ui.screens.defaultTextFieldColors // Assumindo que você moveu para CommonUiComposables.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    mainNavController: NavHostController, // Ainda recebe mainNavController para o logout
    profileViewModel: ProfileViewModel = viewModel() // Injeta ProfileViewModel
) {
    // **MUDANÇA AQUI:** Use collectAsState() para observar o StateFlow
    val uiState by profileViewModel.uiState.collectAsState()

    // Inicia o carregamento do perfil quando a tela é composta
    // Se loadUserProfile já é chamado no init do ViewModel, este LaunchedEffect pode ser removido
    // se você quiser que o carregamento aconteça apenas uma vez.
    // Se o perfil pode ser "recarregado" em outros eventos, mantenha.
    LaunchedEffect(profileViewModel) {
        profileViewModel.loadUserProfile()
    }

    if (uiState.loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        uiState.userData?.let { data ->
            ProfileContent(
                data = data,
                meta = uiState.meta,
                onMetaChange = { profileViewModel.updateMeta(it) },
                onSaveMeta = { profileViewModel.saveMeta() },
                onLogout = {
                    profileViewModel.logoutUser()
                    mainNavController.navigate("login") { // Navega no mainNavController
                        popUpTo(mainNavController.graph.id) { inclusive = true }
                    }
                }
            )
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Erro ao carregar dados do perfil: ${uiState.errorMessage ?: "Desconhecido"}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // <-- Adicione esta anotação aqui
@Composable
fun ProfileContent(
    data: User, // Agora usando a data class User
    meta: String,
    onMetaChange: (String) -> Unit,
    onSaveMeta: () -> Unit, // Callback para salvar a meta
    onLogout: () -> Unit // Callback para logout
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Seu Perfil",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileInfo("Nome", data.nome)
                    ProfileInfo("Idade", data.idade)
                    ProfileInfo("Sexo", data.sexo)
                    ProfileInfo("Peso", data.peso)
                    ProfileInfo("Altura", data.altura)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Meta pessoal",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = meta,
                        onValueChange = onMetaChange,
                        placeholder = { Text("Ex: ganhar massa") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = defaultTextFieldColors() // <-- Esta chamada pode precisar de importação
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onSaveMeta,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Salvar Meta")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Sair")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfo(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}