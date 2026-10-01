package com.example.rgbv10.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Color.oscurecer(factor: Float): Color = Color(red * factor, green * factor, blue * factor, alpha)

/**
 * Estilo "cartoon": degradé suave, contorno oscuro y reflejo brillante arriba a la izquierda.
 */
fun Modifier.cartoon(
    color: Color,
    shape: Shape,
    borderColor: Color = color.oscurecer(0.35f),
    borderWidth: Dp = 2.5.dp,
    reflejo: Boolean = true
): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            listOf(lerp(color, Color.White, 0.15f), color, color.oscurecer(0.8f))
        )
    )
    .border(borderWidth, borderColor, shape)
    .drawWithContent {
        drawContent()
        if (reflejo) {
            val w = size.width
            val h = size.height
            val lado = minOf(w, h)
            val x = w * 0.10f
            val y = h * 0.10f
            val grosor = lado * 0.13f
            drawRoundRect(
                color = Color.White.copy(alpha = 0.5f),
                topLeft = Offset(x, y),
                size = Size(minOf(w * 0.28f, lado), grosor),
                cornerRadius = CornerRadius(grosor / 2)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = grosor * 0.4f,
                center = Offset(x + grosor * 0.4f, y + grosor * 2.1f)
            )
        }
    }

/** Superficie cartoon clickeable que se encoge un poco al presionarla. */
@Composable
fun CartoonBoton(
    color: Color,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = color.oscurecer(0.35f),
    borderWidth: Dp = 2.5.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(if (presionado) 0.94f else 1f, label = "escala")

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
            .cartoon(color, shape, borderColor, borderWidth)
            .clickable(interactionSource = interaccion, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}
