package com.example.rgbv10.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/** Paleta cyberpunk. En tema claro conserva la misma estructura con tonos de panel claro. */
@Immutable
class CyberColores(val oscuro: Boolean) {
    val fondoArriba = if (oscuro) Color(0xFF05060D) else Color(0xFFE9ECF6)
    val fondoAbajo = if (oscuro) Color(0xFF0B0820) else Color(0xFFD9DEEE)
    val panel = if (oscuro) Color(0xFF0C0E1C) else Color(0xFFF6F7FC)
    val tecla = if (oscuro) Color(0xFF151832) else Color(0xFFFFFFFF)
    val teclaSombra = if (oscuro) Color(0xFF070816) else Color(0xFFD3D8EA)
    val cian = if (oscuro) Color(0xFF00E5FF) else Color(0xFF008DA8)
    val magenta = if (oscuro) Color(0xFFFF2BD6) else Color(0xFFB8008A)
    val rojo = if (oscuro) Color(0xFFFF1744) else Color(0xFFD50032)
    val verde = if (oscuro) Color(0xFF00F57A) else Color(0xFF00A152)
    val texto = if (oscuro) Color(0xFFE6F7FF) else Color(0xFF10132B)
    val textoSuave = if (oscuro) Color(0xFF7F8AB8) else Color(0xFF55608A)
    val lineas = cian.copy(alpha = if (oscuro) 0.10f else 0.16f)

    /** Intensidad general del resplandor: más suave en tema claro. */
    val glow = if (oscuro) 1f else 0.55f
}

val FuenteTecnica = FontFamily.Monospace

/** Fondo casi negro con cuadrícula tenue y líneas de escaneo. */
fun Modifier.fondoCyber(c: CyberColores): Modifier = this
    .background(Brush.verticalGradient(listOf(c.fondoArriba, c.fondoAbajo)))
    .drawBehind {
        val paso = 28.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(c.lineas, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += paso
        }
        var y = 0f
        while (y < size.height) {
            drawLine(c.lineas, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += paso
        }
        val scan = Color.Black.copy(alpha = if (c.oscuro) 0.14f else 0.03f)
        val pasoScan = 4.dp.toPx()
        y = 0f
        while (y < size.height) {
            drawLine(scan, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += pasoScan
        }
    }

/** Panel con borde tenue y esquineros estilo HUD. */
fun Modifier.marcoHud(c: CyberColores, acento: Color = c.cian): Modifier = this.drawBehind {
    val r = 14.dp.toPx()
    drawRoundRect(c.panel, cornerRadius = CornerRadius(r))
    drawRoundRect(acento.copy(alpha = 0.25f), cornerRadius = CornerRadius(r), style = Stroke(1.dp.toPx()))

    val largo = 20.dp.toPx()
    val grosor = 2.dp.toPx()
    val w = size.width
    val h = size.height
    val col = acento.copy(alpha = 0.9f)
    fun trazo(x1: Float, y1: Float, x2: Float, y2: Float) =
        drawLine(col, Offset(x1, y1), Offset(x2, y2), grosor, StrokeCap.Round)
    // arriba izquierda / arriba derecha / abajo izquierda / abajo derecha
    trazo(r, 0f, r + largo, 0f); trazo(0f, r, 0f, r + largo)
    trazo(w - r, 0f, w - r - largo, 0f); trazo(w, r, w, r + largo)
    trazo(r, h, r + largo, h); trazo(0f, h - r, 0f, h - r - largo)
    trazo(w - r, h, w - r - largo, h); trazo(w, h - r, w, h - r - largo)
}

/** Resplandor neón: capas translúcidas que se desvanecen hacia afuera. */
fun DrawScope.glowRect(color: Color, radio: Float, intensidad: Float, extension: Float) {
    for (i in 4 downTo 1) {
        val e = extension * i / 4f
        drawRoundRect(
            color = color.copy(alpha = (intensidad * 0.09f).coerceIn(0f, 1f)),
            topLeft = Offset(-e, -e),
            size = Size(size.width + 2 * e, size.height + 2 * e),
            cornerRadius = CornerRadius(radio + e)
        )
    }
}

fun DrawScope.glowCirculo(color: Color, intensidad: Float, extension: Float) {
    val base = size.minDimension / 2f
    for (i in 4 downTo 1) {
        drawCircle(
            color = color.copy(alpha = (intensidad * 0.09f).coerceIn(0f, 1f)),
            radius = base + extension * i / 4f
        )
    }
}

/**
 * Módulo de control iluminado.
 *
 * [lente] es el color real del botón (se ve siempre, es información funcional).
 * [neon] es el color del contorno y del resplandor.
 */
@Composable
fun CyberTecla(
    c: CyberColores,
    neon: Color,
    onClick: () -> Unit,
    descripcion: String,
    modifier: Modifier = Modifier,
    lente: Color? = null,
    activa: Boolean = false,
    brilloBase: Float = 0.35f,
    brilloActivo: Float = 0.8f,
    bordeForzado: Color? = null,
    contenido: @Composable BoxScope.() -> Unit = {}
) {
    val forma = RoundedCornerShape(14.dp)
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()

    // Pulso corto al presionar; las teclas activas quedan iluminadas
    val brillo by animateFloatAsState(
        targetValue = if (presionado) 1.2f else if (activa) brilloActivo else brilloBase,
        label = "brillo"
    )
    val escala by animateFloatAsState(if (presionado) 0.94f else 1f, label = "escala")

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
            .drawBehind { glowRect(neon, 14.dp.toPx(), brillo * c.glow, 10.dp.toPx()) }
            .clip(forma)
            .background(Brush.verticalGradient(listOf(c.tecla, c.teclaSombra)))
            .border(
                width = if (bordeForzado != null) 2.dp else 1.5.dp,
                color = bordeForzado ?: neon.copy(alpha = (0.45f + 0.4f * brillo).coerceAtMost(1f)),
                shape = forma
            )
            .semantics { contentDescription = descripcion }
            .clickable(interactionSource = interaccion, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (lente != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(7.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(lerp(lente, Color.White, 0.3f), lente, lente.oscurecer(0.65f))
                        )
                    )
                    .drawBehind {
                        // Reflejo de luz arriba y sombra interior abajo
                        val ancho = size.width * 0.5f
                        drawRoundRect(
                            Color.White.copy(alpha = 0.35f),
                            topLeft = Offset(size.width * 0.12f, size.height * 0.1f),
                            size = Size(ancho, size.height * 0.1f),
                            cornerRadius = CornerRadius(size.height * 0.05f)
                        )
                        drawRect(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                                startY = size.height * 0.6f
                            )
                        )
                    }
                    .border(1.dp, Color.Black.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
            )
        }
        contenido()
        if (activa) {
            // LED de estado
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(6.dp)
                    .drawBehind { glowCirculo(neon, 1f, 4.dp.toPx()) }
                    .background(Color.White, CircleShape)
            )
        }
    }
}

/** Sol de brillo dibujado a mano: [grande] = brillo +, chico = brillo −. */
@Composable
fun IconoSol(color: Color, grande: Boolean, modifier: Modifier = Modifier.size(30.dp)) {
    Canvas(modifier) {
        val lado = size.minDimension
        val r = lado * if (grande) 0.20f else 0.13f
        val trazo = 2.dp.toPx()
        drawCircle(color, r, center, style = Stroke(trazo))
        val interior = r + 3.dp.toPx()
        val largo = lado * if (grande) 0.16f else 0.10f
        for (i in 0 until 8) {
            val a = Math.toRadians(i * 45.0)
            val dx = cos(a).toFloat()
            val dy = sin(a).toFloat()
            drawLine(
                color,
                center + Offset(dx * interior, dy * interior),
                center + Offset(dx * (interior + largo), dy * (interior + largo)),
                strokeWidth = trazo,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun Led(color: Color, encendido: Boolean = true, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .drawBehind { if (encendido) glowCirculo(color, 1.2f, 5.dp.toPx()) }
            .background(if (encendido) color else color.copy(alpha = 0.25f), CircleShape)
    )
}

@Composable
fun EtiquetaTecnica(c: CyberColores, texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        modifier = modifier,
        color = c.textoSuave,
        fontFamily = FuenteTecnica,
        fontSize = 10.sp,
        letterSpacing = 2.sp
    )
}

/** Botón chico con contorno neón (usado para el cambio de tema). */
@Composable
fun CyberChip(c: CyberColores, texto: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .clip(forma)
            .background(c.tecla)
            .border(1.dp, c.cian.copy(alpha = 0.7f), forma)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            color = c.cian,
            fontFamily = FuenteTecnica,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )
    }
}
