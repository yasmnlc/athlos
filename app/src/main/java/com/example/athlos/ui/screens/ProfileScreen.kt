package com.example.athlos.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.athlos.data.model.User
import com.example.athlos.viewmodels.LoginViewModel
import com.example.athlos.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    mainNavController: NavHostController,
    profileViewModel: ProfileViewModel = viewModel(),
    loginViewModel: LoginViewModel = viewModel()
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(profileViewModel) {
        profileViewModel.loadUserProfile()
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            Log.e("ProfileScreen", "Erro: ${uiState.errorMessage}")
        }
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
                isEditingMeta = uiState.isEditingMeta,
                isSavingMeta = uiState.isSavingMeta,
                isUploadingPhoto = uiState.isUploadingPhoto,
                profileImageUrl = uiState.profileImageUrl,
                onMetaChange = { profileViewModel.updateMeta(it) },
                onToggleEditMeta = { profileViewModel.toggleEditMeta(it) },
                onSaveMeta = { profileViewModel.saveMeta() },
                onUploadPhoto = { uri -> profileViewModel.uploadProfileImage(uri) },
                onLogout = {
                    profileViewModel.logoutUser()
                    loginViewModel.signOutGoogle()
                    mainNavController.navigate("login") {
                        popUpTo(mainNavController.graph.id) { inclusive = true }
                    }
                }
            )
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Erro ao carregar dados do perfil: ${uiState.errorMessage ?: "Desconhecido"}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    data: User,
    meta: String,
    isEditingMeta: Boolean,
    isSavingMeta: Boolean,
    isUploadingPhoto: Boolean,
    profileImageUrl: String?,
    onMetaChange: (String) -> Unit,
    onToggleEditMeta: (Boolean) -> Unit,
    onSaveMeta: () -> Unit,
    onUploadPhoto: (Uri) -> Unit,
    onLogout: () -> Unit
) {
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onUploadPhoto(it) }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val backgroundColor = MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.05f), backgroundColor)
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Foto de Perfil
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .clickable(enabled = !isUploadingPhoto) { pickImageLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUrl != null && profileImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Foto de Perfil",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar padrão",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
            if (isUploadingPhoto) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Alterar foto",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .size(40.dp) // Tamanho maior
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = data.nome,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Seu Perfil",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ProfileInfo("Idade", data.idade)
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                ProfileInfo("Sexo", data.sexo)
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                ProfileInfo("Peso", data.peso)
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                ProfileInfo("Altura", data.altura)

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Meta pessoal",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(onClick = { onToggleEditMeta(!isEditingMeta) }) {
                        Icon(
                            imageVector = if (isEditingMeta) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = if (isEditingMeta) "Salvar Meta" else "Editar Meta",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (isEditingMeta) {
                    OutlinedTextField(
                        value = meta,
                        onValueChange = onMetaChange,
                        placeholder = { Text("Ex: ganhar massa muscular") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = defaultTextFieldColors(),
                        singleLine = true,
                        enabled = !isSavingMeta
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSaveMeta,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = meta.isNotBlank() && !isSavingMeta,
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (isSavingMeta) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Salvar Meta")
                        }
                    }
                } else {
                    Text(
                        text = if (meta.isNotBlank()) meta else "Nenhuma meta definida. Toque no lápis para editar.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Sair da Conta")
                }
            }
        }
    }
}

@Composable
fun ProfileInfo(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Normal
        )
    }
}