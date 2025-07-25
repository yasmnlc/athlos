package com.example.athlos.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.athlos.ui.models.FoodItem
import com.example.athlos.ui.theme.DarkGreen
import com.example.athlos.viewmodels.DiaryViewModel
import com.example.athlos.viewmodels.FoodSearchViewModel
import java.time.LocalDate // Importar LocalDate
import java.time.format.DateTimeFormatter

// Cores para a UI, podem ser movidas para um arquivo de tema se preferir
private val MealTitleBackgroundColor = Color(0xFFE0E0E0)
private val FoodCardBackgroundColor = Color(0xFFF5F5F5)

// Função de tradução simples (como fornecida anteriormente)
fun String.traduzirSimples(): String {
    return this.lowercase()
        .replace("chicken", "frango")
        .replace("beef", "carne bovina")
        .replace("pork", "carne suína")
        .replace("milk", "leite")
        .replace("egg", "ovo")
        .replace("rice", "arroz")
        .replace("bread", "pão")
        .replace("cheese", "queijo")
        .replaceFirstChar { it.uppercaseChar() }
}

@Composable
fun DiaryScreen(
    // A tela agora usa dois ViewModels: um para o estado do diário e outro para a busca de alimentos.
    diaryViewModel: DiaryViewModel = viewModel(),
    foodSearchViewModel: FoodSearchViewModel = viewModel()
) {
    // O estado agora vem diretamente do DiaryViewModel, que lê do banco de dados Room.
    val uiState by diaryViewModel.uiState.collectAsState()
    val allFoods = uiState.foodEntries

    // Os cálculos de totais continuam funcionando, pois reagem às mudanças na lista de alimentos.
    val totalCalories = allFoods.sumOf { it.calories }.toInt()
    val totalCarb     = allFoods.sumOf { it.carbohydrate }
    val totalProtein  = allFoods.sumOf { it.protein }
    val totalFat      = allFoods.sumOf { it.fat }

    // Metas diárias (podem ser movidas para o ViewModel ou configurações do usuário no futuro)
    val calorieGoal = 2000
    val carbGoal = 275.0
    val proteinGoal = 50.0
    val fatGoal = 70.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // O cabeçalho com calorias e macros não precisa de alterações.
        Text(
            text = "$totalCalories kcal",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(DarkGreen)
                .wrapContentSize(Alignment.Center)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MacroProgressItem("CARBOIDRATO", (totalCarb / carbGoal * 100).toInt(), Color(0xFFFFC107))
            MacroProgressItem("PROTEÍNA",   (totalProtein / proteinGoal * 100).toInt(), Color(0xFFD32F2F))
            MacroProgressItem("GORDURA",    (totalFat / fatGoal * 100).toInt(), Color(0xFF4CAF50))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // As seções de refeição agora filtram a lista única vinda do ViewModel.
        // O nome da refeição no filtro deve ser EXATAMENTE igual ao mealType salvo no FoodItem.
        MealSection(
            title = "CAFÉ DA MANHÃ",
            mealType = "CAFÉ DA MANHÃ", // Passa o mealType exato
            foods = allFoods.filter { it.mealType == "CAFÉ DA MANHÃ" },
            diaryViewModel = diaryViewModel,
            foodSearchViewModel = foodSearchViewModel
        )
        MealSection(
            title = "ALMOÇO",
            mealType = "ALMOÇO", // Passa o mealType exato
            foods = allFoods.filter { it.mealType == "ALMOÇO" },
            diaryViewModel = diaryViewModel,
            foodSearchViewModel = foodSearchViewModel
        )
        MealSection(
            title = "JANTAR",
            mealType = "JANTAR", // Passa o mealType exato
            foods = allFoods.filter { it.mealType == "JANTAR" },
            diaryViewModel = diaryViewModel,
            foodSearchViewModel = foodSearchViewModel
        )
        MealSection(
            title = "LANCHES/OUTROS",
            mealType = "LANCHES/OUTROS", // Passa o mealType exato
            foods = allFoods.filter { it.mealType == "LANCHES/OUTROS" },
            diaryViewModel = diaryViewModel,
            foodSearchViewModel = foodSearchViewModel
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MealSection(
    title: String,
    mealType: String, // Adicionado para passar o tipo de refeição para o AlertDialog
    foods: List<FoodItem>, // A lista agora é imutável (List) em vez de MutableList.
    diaryViewModel: DiaryViewModel,
    foodSearchViewModel: FoodSearchViewModel
) {
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var foodToEdit by remember { mutableStateOf<FoodItem?>(null) }
    // uiState não é mais usado diretamente aqui, pois foods já é filtrado.

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MealTitleBackgroundColor, RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showSearchDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar alimento", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (showSearchDialog) {
            AlertDialog(
                onDismissRequest = { showSearchDialog = false },
                title = { Text("Buscar alimento") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Digite o alimento") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { foodSearchViewModel.searchFood(searchQuery) }) {
                            Text("Buscar")
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        when {
                            foodSearchViewModel.isLoading -> CircularProgressIndicator()
                            foodSearchViewModel.errorMessage != null -> Text(foodSearchViewModel.errorMessage ?: "Erro", color = Color.Red)
                            else -> Column {
                                // Mapeia os resultados da busca para o modelo de dados do BD
                                foodSearchViewModel.foodList.forEach { searchResult ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = FoodCardBackgroundColor),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        onClick = {
                                            // AÇÃO ATUALIZADA: Adiciona o alimento através do DiaryViewModel.
                                            val newEntry = FoodItem(
                                                // UUID é gerado automaticamente no FoodItem, não precisa aqui
                                                date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), // Garante a data correta
                                                mealType = mealType, // Usa o mealType passado para a MealSection
                                                name = searchResult.name,
                                                grams = searchResult.grams,
                                                baseCalories = searchResult.baseCalories,
                                                baseProtein = searchResult.baseProtein,
                                                baseCarbohydrate = searchResult.baseCarbohydrate,
                                                baseFat = searchResult.baseFat,
                                                baseFiber = searchResult.baseFiber
                                            )
                                            diaryViewModel.addFood(newEntry)
                                            showSearchDialog = false
                                            searchQuery = ""
                                            foodSearchViewModel.clearResults()
                                        }
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(text = searchResult.name.traduzirSimples(), style = MaterialTheme.typography.titleMedium) // Usar tradução aqui
                                            // As propriedades quantity e unit não existem no modelo FoodItem, usar grams
                                            Text(text = "${searchResult.grams} g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Prot: %.1fg | Carb: %.1fg | Fib: %.1fg | Gord: %.1fg | %.0fkcal".format(
                                                    searchResult.baseProtein, searchResult.baseCarbohydrate, searchResult.baseFiber, searchResult.baseFat, searchResult.baseCalories.toFloat()
                                                ),
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = {
                        foodSearchViewModel.clearResults()
                        showSearchDialog = false
                    }) {
                        Text("Fechar")
                    }
                }
            )
        }

        foodToEdit?.let { food ->
            EditFoodDialog(
                food = food,
                onDismiss = { foodToEdit = null },
                onSave = { updatedGrams ->
                    // AÇÃO ATUALIZADA: Atualiza o alimento através do DiaryViewModel.
                    diaryViewModel.updateFood(food.copy(grams = updatedGrams))
                    foodToEdit = null
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (foods.isEmpty()) {
            Text(
                text = "Nenhum alimento adicionado. Clique no '+' para adicionar.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        } else {
            foods.forEach { food ->
                // Usar o novo Composable FoodItemCardWithActions para exibir os cards
                EditableFoodItemCard(
                    food = food,
                    onEditClick = { foodToEdit = food },
                    // AÇÃO ATUALIZADA: Apaga o alimento através do DiaryViewModel.
                    onDeleteClick = { diaryViewModel.deleteFood(food) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// Composables auxiliares permanecem inalterados.

@Composable
fun MacroProgressItem(label: String, progress: Int, progressColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val strokeWidth = 8.dp
        ProgressRing(progress, progressColor, strokeWidth)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun ProgressRing(progress: Int, progressColor: Color, strokeWidth: Dp) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceIn(0, 100) / 100f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    val backgroundRingColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

    Canvas(modifier = Modifier.size(80.dp)) {
        val radius = size.minDimension / 2f

        drawCircle(
            color = backgroundRingColor,
            radius = radius,
            style = Stroke(width = strokeWidth.toPx())
        )

        drawArc(
            color = progressColor,
            startAngle = 270f,
            sweepAngle = animatedProgress.value * 360f,
            useCenter = false,
            topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2),
            size = Size(
                width = size.width - strokeWidth.toPx(),
                height = size.height - strokeWidth.toPx()
            ),
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}


@Composable
fun EditableFoodItemCard(
    food: FoodItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FoodCardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name.traduzirSimples(), // Usar a função de tradução aqui
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${food.grams} g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Prot: %.1fg | Carb: %.1fg | Gord: %.1fg | %.0fkcal".format(
                        food.protein, food.carbohydrate, food.fat, food.calories
                    ),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun EditFoodDialog(
    food: FoodItem,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var grams by remember { mutableStateOf(food.grams.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Quantidade") },
        text = {
            Column {
                Text("Alimento: ${food.name}")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = grams,
                    onValueChange = { grams = it.filter { char -> char.isDigit() } },
                    label = { Text("Quantidade (g)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newGrams = grams.toIntOrNull() ?: food.grams
                    onSave(newGrams)
                },
                enabled = grams.isNotBlank()
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}