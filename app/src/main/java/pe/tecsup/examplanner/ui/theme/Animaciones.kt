package pe.tecsup.examplanner.ui.theme

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable

/**
 * Animación de entrada para items de listas: aparecen con fade + leve
 * desplazamiento hacia arriba, escalonados según su índice.
 *
 * Uso:  Modifier.entradaAnimada(index)
 */
@Composable
fun Modifier.entradaAnimada(index: Int = 0): Modifier {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        // pequeño retardo escalonado por posición (máx 6 items de stagger)
        val delay = (index.coerceAtMost(6) * 60).toLong()
        kotlinx.coroutines.delay(delay)
        progreso.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 380, easing = EaseOutCubic)
        )
    }
    return this
        .alpha(progreso.value)
        .graphicsLayer {
            translationY = (1f - progreso.value) * 40f
        }
}

/**
 * Efecto "shimmer" / pulso de opacidad para placeholders de carga.
 */
@Composable
fun rememberPulso(): Float {
    val transition = rememberInfiniteTransition(label = "pulso")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsoAlpha"
    )
    return alpha
}
