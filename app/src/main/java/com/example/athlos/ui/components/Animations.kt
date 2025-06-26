package com.example.athlos.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

@Composable
fun FadeInBox(isVisible: Boolean, duration: Int = 500, content: @Composable BoxScope.() -> Unit) {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = duration), label = "fade"
    )

    androidx.compose.foundation.layout.Box(modifier = Modifier.alpha(alpha), content = content)
}