package com.example.athlos.ui.screens

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.athlos.api.RetrofitInstance
import com.example.athlos.ui.models.FoodItem
import com.example.athlos.ui.theme.DarkGreen
import com.example.athlos.viewmodel.FoodSearchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val MealTitleBackgroundColor = Color(0xFFE0E0E0)
private val FoodCardBackgroundColor = Color(0xFFF5F5F5)

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
fun DiaryScreen(viewModel: FoodSearchViewModel = viewModel()) {
    val breakfastFoods = remember { mutableStateListOf<FoodItem>() }
    val lunchFoods     = remember { mutableStateListOf<FoodItem>() }
    val dinnerFoods    = remember { mutableStateListOf<FoodItem>() }
    val snacksFoods    = remember { mutableStateListOf<FoodItem>() }

    val context   = LocalContext.current
    val scope     = rememberCoroutineScope()
    val edamamApi = RetrofitInstance.api

    val allFoods = remember { derivedStateOf {
        breakfastFoods + lunchFoods + dinnerFoods + snacksFoods
    } }

    val totalCalories = allFoods.value.sumOf { it.calories.toDouble() }.toInt()
    val totalCarb     = allFoods.value.sumOf { it.carbohydrate.toDouble() }
    val totalProtein  = allFoods.value.sumOf { it.protein.toDouble() }
    val totalFat      = allFoods.value.sumOf { it.fat.toDouble() }

    val calorieGoal = 2000
    val carbGoal    = 275.0
    val proteinGoal = 50.0
    val fatGoal     = 70.0

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

        MealSection("CAFÉ DA MANHÃ", breakfastFoods, scope, context, edamamApi, viewModel)
        MealSection("ALMOÇO",        lunchFoods,     scope, context, edamamApi, viewModel)
        MealSection("JANTAR",        dinnerFoods,    scope, context, edamamApi, viewModel)
        MealSection("LANCHES/OUTROS", snacksFoods,   scope, context, edamamApi, viewModel)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

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
fun MealSection(
    title: String,
    foods: MutableList<FoodItem>,
    scope: CoroutineScope,
    context: Context,
    edamamApi: com.example.athlos.api.EdamamApi,
    viewModel: FoodSearchViewModel
) {
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

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
                        Button(onClick = {
                            viewModel.searchFood(searchQuery)
                        }) {
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
                                                text = result.name.traduzirSimples(),
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "${result.quantity} ${result.unit}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Prot: %.1fg | Carb: %.1fg | Fib: %.1fg | Gord: %.1fg | %.0fkcal".format(
                                                    result.protein, result.carbohydrate, result.fiber, result.fat, result.calories
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
                FoodItemCard(food)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun FoodItemCard(food: FoodItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FoodCardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = food.name,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${food.quantity} ${food.unit}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Prot: %.2fg | Carb: %.2fg | Fib: %.2fg | Gord: %.2fg | %.0fkcal".format(
                    food.protein, food.carbohydrate, food.fiber, food.fat, food.calories
                ),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
