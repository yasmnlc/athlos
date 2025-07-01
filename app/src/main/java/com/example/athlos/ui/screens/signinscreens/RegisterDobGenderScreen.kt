package com.example.athlos.ui.screens.signinscreens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.example.athlos.R
import java.util.Calendar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PersonOutline
import android.widget.Toast
import com.example.athlos.viewmodels.RegisterViewModel
import com.example.athlos.ui.screens.defaultTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDobGenderScreen(navController: NavHostController, viewModel: RegisterViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var dataNascimentoState by remember { mutableStateOf(TextFieldValue(uiState.dataNascimentoText)) }
    LaunchedEffect(uiState.dataNascimentoText) {
        if (dataNascimentoState.text != uiState.dataNascimentoText) {
            dataNascimentoState = TextFieldValue(uiState.dataNascimentoText, selection = TextRange(uiState.dataNascimentoText.length))
        }
    }

    var sexo by remember { mutableStateOf(uiState.sexo) }
    LaunchedEffect(uiState.sexo) { sexo = uiState.sexo }

    var sexoExpanded by remember { mutableStateOf(false) }
    val opcoesSexo = listOf("Masculino", "Feminino", "Outro")

    var showDobYearError by remember { mutableStateOf(false) }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.walkingdude))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        speed = 1f
    )

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
            val formattedDate = String.format("%02d/%02d/%d", selectedDayOfMonth, selectedMonth + 1, selectedYear)
            viewModel.updateDataNascimento(formattedDate)
            showDobYearError = !viewModel.isDobValid()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

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
            modifier = Modifier.size(200.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Qual sua data de nascimento e sexo?",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Isso nos ajuda a personalizar sua experiência!",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = dataNascimentoState,
            onValueChange = { newValue ->
                val filteredText = newValue.text.filter { char -> char.isDigit() }.take(8)
                var formatted = ""
                // Lógica de formatação DD/MM/YYYY
                if (filteredText.length > 0) formatted += filteredText.substring(0, minOf(2, filteredText.length))
                if (filteredText.length > 2) formatted += "/" + filteredText.substring(2, minOf(4, filteredText.length))
                if (filteredText.length > 4) formatted += "/" + filteredText.substring(4, minOf(8, filteredText.length))

                dataNascimentoState = TextFieldValue(text = formatted, selection = TextRange(formatted.length))

                viewModel.updateDataNascimento(formatted)

                showDobYearError = !viewModel.isDobValid()
            },
            label = { Text("Data de nascimento (DD/MM/AAAA)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            trailingIcon = {
                IconButton(onClick = {
                    datePickerDialog.show()
                    showDobYearError = false
                }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Selecionar Data")
                }
            },
            colors = defaultTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        if (showDobYearError && !viewModel.isDobValid()) {
            Text(
                text = "Data inválida ou ano futuro. Por favor, insira uma data anterior ao ano atual (DD/MM/AAAA).",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start).padding(start = 16.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = sexoExpanded,
            onExpandedChange = { sexoExpanded = !sexoExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = sexo,
                onValueChange = { },
                readOnly = true,
                label = { Text("Sexo") },
                placeholder = { Text("Selecione seu sexo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexoExpanded) },
                leadingIcon = { Icon(Icons.Default.PersonOutline, contentDescription = "Sexo") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = defaultTextFieldColors()
            )

            ExposedDropdownMenu(expanded = sexoExpanded, onDismissRequest = { sexoExpanded = false }) {
                opcoesSexo.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            sexo = option
                            viewModel.updateSexo(option)
                            sexoExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (viewModel.isDobValid() && viewModel.isGenderValid()) {
                    navController.navigate("register_weight_height")
                } else {
                    val message = when {
                        !viewModel.isDobValid() -> "Por favor, insira uma data de nascimento válida (DD/MM/AAAA)."
                        !viewModel.isGenderValid() -> "Por favor, selecione seu sexo."
                        else -> "Preencha todos os campos corretamente."
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            },
            enabled = viewModel.isDobValid() && viewModel.isGenderValid(),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Próximo")
        }
    }
}