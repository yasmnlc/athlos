package com.example.athlos.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin
import androidx.compose.runtime.*
import java.time.LocalDate

@Composable
fun WaterScreen() {
    val today = remember { LocalDate.now() }
    var lastDate by rememberSaveable { mutableStateOf(today) }
    var waterIntake by rememberSaveable { mutableStateOf(0) }

    val minGoal = 3000       // 3 litros (meta mínima para visualização)
    val maxGoal = 10000      // 10 litros (máximo que o usuário pode registrar)

    LaunchedEffect(key1 = today) {
        if (today != lastDate) {
            waterIntake = 0
            lastDate = today
        }
    }

    val waveShiftAnim = rememberInfiniteTransition()
        .animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 150000, easing = LinearEasing)
            )
        )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Aqui, progress varia entre 0 e 1 para a faixa entre minGoal e maxGoal
        val progress = when {
            waterIntake < minGoal -> 0f
            waterIntake >= maxGoal -> 1f
            else -> (waterIntake - minGoal) / (maxGoal - minGoal).toFloat()
        }.coerceIn(0f, 1f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val waterHeight = progress * height

            val waveHeight = 30f
            val waveLength = width / 1.5f
            val frequency = (2 * Math.PI / waveLength).toFloat()
            val phaseShift = waveShiftAnim.value * waveLength * 2

            val waterTop = height - (progress * height)

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

            val waterIntakeLiters = waterIntake / 1000f

            Text(
                text = String.format("%.2f L", waterIntakeLiters),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (waterIntake + 250 <= maxGoal) waterIntake += 250
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("+250ml")
                }

                Button(
                    onClick = {
                        if (waterIntake + 500 <= maxGoal) waterIntake += 500
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("+500ml")
                }
            }
        }
    }
}

