package com.example.athlos.ui.screens

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.athlos.ui.models.FoodItem
import com.example.athlos.ui.theme.DarkGreen
import com.example.athlos.viewmodels.FoodSearchViewModel
import kotlinx.coroutines.CoroutineScope

private val MealTitleBackgroundColor = Color(0xFFE0E0E0)
private val FoodCardBackgroundColor = Color(0xFFF5F5F5)

@Composable
fun DiaryScreen(viewModel: FoodSearchViewModel = viewModel()) {
    val breakfastFoods = remember { mutableStateListOf<FoodItem>() }
    val lunchFoods     = remember { mutableStateListOf<FoodItem>() }
    val dinnerFoods    = remember { mutableStateListOf<FoodItem>() }
    val snacksFoods    = remember { mutableStateListOf<FoodItem>() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val allFoods = remember {
        derivedStateOf {
            breakfastFoods + lunchFoods + dinnerFoods + snacksFoods
        }
    }

    // O cálculo total agora usa as propriedades dinâmicas do FoodItem,
    // então ele se ajustará automaticamente quando a quantidade for editada.
    val totalCalories = allFoods.value.sumOf { it.calories.toDouble() }.toInt()
    val totalCarb     = allFoods.value.sumOf { it.carbohydrate }
    val totalProtein  = allFoods.value.sumOf { it.protein }
    val totalFat      = allFoods.value.sumOf { it.fat }

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

        MealSection("CAFÉ DA MANHÃ", breakfastFoods, scope, context, viewModel)
        MealSection("ALMOÇO",        lunchFoods,     scope, context, viewModel)
        MealSection("JANTAR",        dinnerFoods,    scope, context, viewModel)
        MealSection("LANCHES/OUTROS", snacksFoods,   scope, context, viewModel)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MealSection(
    title: String,
    foods: MutableList<FoodItem>,
    scope: CoroutineScope,
    context: Context,
    viewModel: FoodSearchViewModel
) {
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Estado para controlar o diálogo de edição
    var foodToEdit by remember { mutableStateOf<FoodItem?>(null) }

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

        // Diálogo de busca de alimentos
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
                        Button(onClick = { viewModel.searchFood(searchQuery) }) {
                            Text("Buscar")
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        when {
                            viewModel.isLoading -> CircularProgressIndicator()
                            viewModel.errorMessage != null -> Text(viewModel.errorMessage ?: "Erro", color = Color.Red)
                            else -> Column {
                                viewModel.foodList.forEach { result ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = FoodCardBackgroundColor),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        onClick = {
                                            foods.add(result)
                                            showSearchDialog = false
                                            searchQuery = ""
                                            viewModel.clearResults()
                                        }
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = result.name,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "100 g", // O padrão é sempre 100g
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Prot: %.1fg | Carb: %.1fg | Fib: %.1fg | Gord: %.1fg | %.0fkcal".format(
                                                    result.baseProtein, result.baseCarbohydrate, result.baseFiber, result.baseFat, result.baseCalories.toFloat()
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
                    TextButton(onClick = { showSearchDialog = false }) {
                        Text("Fechar")
                    }
                }
            )
        }

        // Diálogo para editar a quantidade do alimento
        foodToEdit?.let { food ->
            EditFoodDialog(
                food = food,
                onDismiss = { foodToEdit = null },
                onSave = { updatedGrams ->
                    val index = foods.indexOfFirst { it.id == food.id }
                    if (index != -1) {
                        foods[index] = food.copy(grams = updatedGrams)
                    }
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
            // Lista de alimentos adicionados com botões de editar/apagar
            foods.forEach { food ->
                EditableFoodItemCard(
                    food = food,
                    onEditClick = { foodToEdit = food },
                    onDeleteClick = { foods.remove(food) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
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
                    text = food.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${food.grams} g", // Mostra a quantidade atual
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    // Mostra os macros calculados para a quantidade atual
                    text = "Prot: %.1fg | Carb: %.1fg | Gord: %.1fg | %.0fkcal".format(
                        food.protein, food.carbohydrate, food.fat, food.calories.toFloat()
                    ),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Botões de Ação
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


// As funções MacroProgressItem e ProgressRing permanecem inalteradas
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