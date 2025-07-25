package com.example.athlos.ui.screens

import android.app.Application
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.athlos.data.repository.FirebaseAuthRepository
import com.example.athlos.viewmodels.WaterViewModel
import com.example.athlos.viewmodels.WaterViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val authRepository = FirebaseAuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

    val factory = WaterViewModelFactory(application, authRepository)

    val waterViewModel: WaterViewModel = viewModel(factory = factory)
    val uiState by waterViewModel.uiState.collectAsState()

    val waveShiftAnim = rememberInfiniteTransition()
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 150000, easing = LinearEasing)
            )
        )

    val progressVisual = (uiState.aguaAtual / uiState.aguaMeta.toFloat()).coerceIn(0f, 1f)

    val waterColor = Color(0xFF2196F3)

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val topAppBarHeightPx = paddingValues.calculateTopPadding().toPx()

                val waveDrawingStartHeight = topAppBarHeightPx
                val waveHeightRange = height - waveDrawingStartHeight

                val waterTop = waveDrawingStartHeight + (waveHeightRange * (1f - progressVisual))

                val waveHeight = 30f
                val waveLength = width / 1.5f
                val frequency = (2 * Math.PI / waveLength).toFloat()
                val phaseShift = waveShiftAnim.value * waveLength * 2

                val path = Path().apply {
                    moveTo(0f, height)
                    lineTo(0f, waterTop)

                    for (x in 0..width.toInt()) {
                        val y = (waveHeight * sin(frequency * x + phaseShift)) + waterTop
                        lineTo(x.toFloat(), y)
                    }

                    lineTo(width, height)
                    close()
                }

                drawPath(
                    path = path,
                    color = waterColor,
                    style = Fill
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                val waterIntakeLiters = uiState.aguaAtual / 1000f

                Text(
                    text = String.format("%.2f L", waterIntakeLiters),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format("de %.2f litros", uiState.aguaMeta.toFloat()),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { waterViewModel.addWater(250) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("+250ml")
                    }

                    Button(
                        onClick = { waterViewModel.addWater(500) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("+500ml")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { waterViewModel.addWater(1000) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("+1000ml")
                    }
                }
            }
        }
    }
}