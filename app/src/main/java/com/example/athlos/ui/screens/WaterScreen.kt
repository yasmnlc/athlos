// app/src/main/java/com/example/athlos/ui/screens/WaterScreen.kt
package com.example.athlos.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.athlos.viewmodel.WaterViewModel
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterScreen(
    waterViewModel: WaterViewModel = viewModel()
) {
    val uiState by waterViewModel.uiState.collectAsState()

    val waveShiftAnim = rememberInfiniteTransition()
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 150000, easing = LinearEasing)
            )
        )

    val progress = (uiState.aguaAtual / uiState.aguaMeta.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val waterTop = height - (progress * height)
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
                color = Color(0xFF2196F3),
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
            Text(
                text = "Controle de Água",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 32.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            val waterIntakeLiters = uiState.aguaAtual / 1000f

            Text(
                text = String.format("%.2f litros", waterIntakeLiters),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center,
                fontSize = 48.sp
            )
            Text(
                text = "/ ${uiState.aguaMeta} ml",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { waterViewModel.addWater(250) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("+250ml")
                }

                Button(
                    onClick = { waterViewModel.addWater(500) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("+500ml")
                }
                Button(
                    onClick = { waterViewModel.addWater(1000) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("+1000ml")
                }
            }
        }
    }
}