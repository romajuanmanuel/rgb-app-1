package com.example.rgbv10

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rgbv10.data.infrared.Tecla
import com.example.rgbv10.data.infrared.TeclasControl
import com.example.rgbv10.data.viewmodel.ControlViewModel
import com.example.rgbv10.ui.components.CyberChip
import com.example.rgbv10.ui.components.CyberColores
import com.example.rgbv10.ui.components.CyberTecla
import com.example.rgbv10.ui.components.EtiquetaTecnica
import com.example.rgbv10.ui.components.FuenteTecnica
import com.example.rgbv10.ui.components.IconoSol
import com.example.rgbv10.ui.components.Led
import com.example.rgbv10.ui.components.fondoCyber
import com.example.rgbv10.ui.components.glowCirculo
import com.example.rgbv10.ui.components.marcoHud
import com.example.rgbv10.ui.theme.RGBV10Theme

class MainActivity : ComponentActivity() {
    private val viewModel: ControlViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        // El widget pudo cambiar el estado de las luces mientras la app estaba en segundo plano
        viewModel.sincronizar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = this.viewModel
            val modoOscuro by viewModel.modoOscuro.collectAsState()

            // Los íconos de las barras del sistema siguen al tema de la app, no al del sistema
            DisposableEffect(modoOscuro) {
                val transparente = android.graphics.Color.TRANSPARENT
                val estilo = if (modoOscuro) SystemBarStyle.dark(transparente)
                else SystemBarStyle.light(transparente, transparente)
                enableEdgeToEdge(statusBarStyle = estilo, navigationBarStyle = estilo)
                onDispose {}
            }

            RGBV10Theme(darkTheme = modoOscuro) {
                PantallaControl(modoOscuro = modoOscuro, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PantallaControl(
    modoOscuro: Boolean,
    modifier: Modifier = Modifier,
    viewModel: ControlViewModel = viewModel()
) {
    val teclaActual by viewModel.teclaActual.collectAsState()
    val encendida by viewModel.encendida.collectAsState()
    val efectoActivo by viewModel.efectoActivo.collectAsState()

    PantallaControlContenido(
        tieneIR = viewModel.tieneIR,
        modoOscuro = modoOscuro,
        encendida = encendida,
        teclaActual = teclaActual,
        efectoActivo = efectoActivo,
        onAlternarTema = viewModel::alternarTema,
        onEncender = viewModel::encender,
        onApagar = viewModel::apagar,
        onSubirBrillo = viewModel::subirBrillo,
        onBajarBrillo = viewModel::bajarBrillo,
        onTecla = viewModel::enviarTecla,
        modifier = modifier
    )
}

/** Contenido sin ViewModel: permite verlo en el Preview. */
@Composable
fun PantallaControlContenido(
    tieneIR: Boolean,
    modoOscuro: Boolean,
    encendida: Boolean,
    teclaActual: Tecla?,
    efectoActivo: Tecla?,
    onAlternarTema: () -> Unit,
    onEncender: () -> Unit,
    onApagar: () -> Unit,
    onSubirBrillo: () -> Unit,
    onBajarBrillo: () -> Unit,
    onTecla: (Tecla) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = remember(modoOscuro) { CyberColores(modoOscuro) }

    Box(modifier = modifier
        .fillMaxSize()
        .fondoCyber(c)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EstadoActual(c, teclaActual, efectoActivo, onAlternarTema)
            EstadoIR(c, tieneIR)
            PanelMando(
                c = c,
                encendida = encendida,
                teclaActual = teclaActual,
                efectoActivo = efectoActivo,
                onEncender = onEncender,
                onApagar = onApagar,
                onSubirBrillo = onSubirBrillo,
                onBajarBrillo = onBajarBrillo,
                onTecla = onTecla
            )
        }
    }
}

private fun Tecla.colorLuz() = Color(rojo, verde, azul)

private fun Tecla.esBlanco() = rojo == 255 && verde == 255 && azul == 255

/** Neón de una tecla: el propio color, un poco más claro para que se note sobre fondo oscuro. */
private fun Color.neon(): Color = lerp(this, Color.White, 0.2f)

private fun Tecla.neon(): Color = colorLuz().neon()

@Composable
private fun EstadoActual(c: CyberColores, tecla: Tecla?, efecto: Tecla?, onAlternarTema: () -> Unit) {
    val color = tecla?.colorLuz()
    // En tema claro el blanco se pierde con el fondo: contorno negro
    val borde = if (tecla?.esBlanco() == true && !c.oscuro) Color.Black else (color?.neon() ?: c.textoSuave)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .marcoHud(c)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (efecto != null) {
            // Con un efecto activo, el círculo muestra el efecto en movimiento
            MuestraEfecto(c, efecto, Modifier.size(44.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .drawBehind { if (color != null) glowCirculo(color.neon(), c.glow, 8.dp.toPx()) }
                    .clip(CircleShape)
                    .background(color ?: c.tecla)
                    .border(2.dp, borde, CircleShape)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            EtiquetaTecnica(c, if (efecto != null) "// EFECTO ACTIVO" else "// COLOR ACTUAL")
            NombreActual(c, (efecto?.nombre ?: tecla?.nombre ?: "SIN SELECCIONAR").uppercase())
        }
        CyberChip(c, if (c.oscuro) "TEMA CLARO" else "TEMA OSCURO", onAlternarTema)
    }
}

/**
 * Nombre del color o efecto actual. Mantiene 18 sp y solo achica si no entra en una línea
 * (por ejemplo "AMARILLO OSCURO"); la altura es fija para que el panel no se mueva.
 */
@Composable
private fun NombreActual(c: CyberColores, texto: String) {
    var tamano by remember(texto) { mutableFloatStateOf(18f) }
    Box(modifier = Modifier.height(24.dp), contentAlignment = Alignment.CenterStart) {
        Text(
            text = texto,
            color = c.texto,
            fontFamily = FuenteTecnica,
            fontWeight = FontWeight.Bold,
            fontSize = tamano.sp,
            letterSpacing = 1.sp,
            maxLines = 1,
            softWrap = false,
            onTextLayout = { if (it.hasVisualOverflow && tamano > 10f) tamano -= 1f }
        )
    }
}

private fun colorTecla(nombre: String): Color =
    TeclasControl.COLORES.first { it.nombre == nombre }.colorLuz()

// Colores de la paleta del control usados por la muestra de cada efecto
private val PALETA_FLASH = listOf("Rojo", "Verde", "Azul", "Amarillo", "Cian", "Magenta").map(::colorTecla)
private val PALETA_FADE = listOf("Rojo", "Verde", "Azul").map(::colorTecla)
private val PALETA_SMOOTH =
    listOf("Rojo", "Naranja", "Amarillo", "Verde", "Cian", "Azul", "Violeta", "Magenta").map(::colorTecla)

/** Color e intensidad (0..1) de la muestra en el instante [fase] (0..1 = 6 s). */
private fun muestraEfecto(nombre: String, fase: Float, c: CyberColores): Pair<Color, Float> = when (nombre) {
    // Flash: salta de un color a otro sin transición
    "Flash" -> PALETA_FLASH[(fase * PALETA_FLASH.size).toInt().coerceAtMost(PALETA_FLASH.size - 1)] to 1f
    // Strobe: destellos rápidos encendido/apagado
    "Strobe" -> c.texto to if ((fase * 50).toInt() % 2 == 0) 1f else 0f
    // Fade: cada color se enciende y se apaga suavemente
    "Fade" -> {
        val tramo = fase * 6
        PALETA_FADE[tramo.toInt() % PALETA_FADE.size] to sin(Math.PI * (tramo - tramo.toInt())).toFloat()
    }
    // Smooth: recorre el espectro con transición continua
    else -> {
        val pos = fase * PALETA_SMOOTH.size
        val i = pos.toInt().coerceAtMost(PALETA_SMOOTH.size - 1)
        lerp(PALETA_SMOOTH[i], PALETA_SMOOTH[(i + 1) % PALETA_SMOOTH.size], pos - i) to 1f
    }
}

@Composable
private fun MuestraEfecto(c: CyberColores, efecto: Tecla, modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "efecto")
    val fase = transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "fase"
    )
    Canvas(modifier) {
        val (color, intensidad) = muestraEfecto(efecto.nombre, fase.value, c)
        val neon = color.neon()
        glowCirculo(neon, c.glow * intensidad, 8.dp.toPx())
        drawCircle(c.tecla)
        drawCircle(color.copy(alpha = intensidad))
        drawCircle(neon, radius = size.minDimension / 2 - 1.dp.toPx(), style = Stroke(2.dp.toPx()))
    }
}

@Composable
private fun EstadoIR(c: CyberColores, tieneIR: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Led(if (tieneIR) c.verde else c.rojo)
        Text(
            text = if (tieneIR) "EMISOR IR ONLINE" else "SIN EMISOR IR: LOS COMANDOS NO SE ENVIAN",
            color = if (tieneIR) c.textoSuave else c.rojo,
            fontFamily = FuenteTecnica,
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
    }
}

/**
 * Misma disposición que el control físico (4 columnas x 6 filas):
 * brillo+ / brillo- / OFF / ON, luego R G B W y, debajo, tres colores + un efecto por fila.
 */
@Composable
private fun PanelMando(
    c: CyberColores,
    encendida: Boolean,
    teclaActual: Tecla?,
    efectoActivo: Tecla?,
    onEncender: () -> Unit,
    onApagar: () -> Unit,
    onSubirBrillo: () -> Unit,
    onBajarBrillo: () -> Unit,
    onTecla: (Tecla) -> Unit
) {
    val colores = TeclasControl.COLORES
    // Con un efecto activo ya no hay un color fijo seleccionado
    val colorSeleccionado = if (efectoActivo == null) teclaActual else null
    val celda = Modifier.aspectRatio(1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .marcoHud(c)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            EtiquetaTecnica(c, "// CONTROL MATRIX")
            EtiquetaTecnica(c, "24 KEYS")
        }

        FilaMando {
            CyberTecla(
                c = c, neon = c.cian, onClick = onSubirBrillo, descripcion = "Subir brillo",
                modifier = Modifier.weight(1f).then(celda)
            ) { IconoSol(c.cian, grande = true) }
            CyberTecla(
                c = c, neon = c.cian, onClick = onBajarBrillo, descripcion = "Bajar brillo",
                modifier = Modifier.weight(1f).then(celda)
            ) { IconoSol(c.cian, grande = false) }
            CyberTecla(
                c = c, neon = c.rojo, onClick = onApagar, descripcion = "Apagar",
                activa = !encendida, brilloBase = 0.12f, brilloActivo = 0.45f,
                modifier = Modifier.weight(1f).then(celda)
            ) { TextoTecla("OFF", if (encendida) c.textoSuave else c.rojo) }
            CyberTecla(
                c = c, neon = c.rojo, onClick = onEncender, descripcion = "Encender",
                activa = encendida, brilloBase = 0.3f, brilloActivo = 1.1f,
                modifier = Modifier.weight(1f).then(celda)
            ) { TextoTecla("ON", c.rojo) }
        }

        FilaMando {
            colores.subList(0, 4).forEach { TeclaColor(c, it, it == colorSeleccionado, onTecla, Modifier.weight(1f).then(celda)) }
        }

        TeclasControl.EFECTOS.forEachIndexed { i, efecto ->
            FilaMando {
                colores.subList(4 + i * 3, 7 + i * 3).forEach {
                    TeclaColor(c, it, it == colorSeleccionado, onTecla, Modifier.weight(1f).then(celda))
                }
                CyberTecla(
                    c = c, neon = c.magenta, onClick = { onTecla(efecto) }, descripcion = efecto.nombre,
                    activa = efecto == efectoActivo,
                    modifier = Modifier.weight(1f).then(celda)
                ) { TextoTecla(efecto.nombre.uppercase(), c.magenta, 11.sp) }
            }
        }
    }
}

@Composable
private fun FilaMando(contenido: @Composable RowScope.() -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
        content = contenido
    )
}

@Composable
private fun TeclaColor(
    c: CyberColores,
    tecla: Tecla,
    seleccionada: Boolean,
    onTecla: (Tecla) -> Unit,
    modifier: Modifier
) {
    CyberTecla(
        c = c,
        neon = tecla.neon(),
        lente = tecla.colorLuz(),
        onClick = { onTecla(tecla) },
        descripcion = tecla.nombre,
        activa = seleccionada,
        // Blanco sobre fondo claro: borde negro para que no se pierda
        bordeForzado = if (tecla.esBlanco() && !c.oscuro) Color.Black else null,
        modifier = modifier
    )
}

@Composable
private fun TextoTecla(texto: String, color: Color, tamano: androidx.compose.ui.unit.TextUnit = 16.sp) {
    Text(
        text = texto,
        color = color,
        fontFamily = FuenteTecnica,
        fontWeight = FontWeight.Bold,
        fontSize = tamano,
        letterSpacing = 1.sp,
        textAlign = TextAlign.Center
    )
}

@Preview(showBackground = true, name = "Oscuro", heightDp = 800)
@Composable
private fun PreviewOscuro() {
    RGBV10Theme(darkTheme = true, dynamicColor = false) {
        PantallaControlContenido(
            tieneIR = true,
            modoOscuro = true,
            encendida = true,
            teclaActual = TeclasControl.COLORES[2],
            efectoActivo = null,
            onAlternarTema = {}, onEncender = {}, onApagar = {},
            onSubirBrillo = {}, onBajarBrillo = {}, onTecla = {}
        )
    }
}

@Preview(showBackground = true, name = "Claro", heightDp = 800)
@Composable
private fun PreviewClaro() {
    RGBV10Theme(darkTheme = false, dynamicColor = false) {
        PantallaControlContenido(
            tieneIR = false,
            modoOscuro = false,
            encendida = false,
            teclaActual = TeclasControl.COLORES[3],
            efectoActivo = null,
            onAlternarTema = {}, onEncender = {}, onApagar = {},
            onSubirBrillo = {}, onBajarBrillo = {}, onTecla = {}
        )
    }
}

@Preview(showBackground = true, name = "Amarillo oscuro", heightDp = 800)
@Composable
private fun PreviewColorLargo() {
    RGBV10Theme(darkTheme = true, dynamicColor = false) {
        PantallaControlContenido(
            tieneIR = true,
            modoOscuro = true,
            encendida = true,
            teclaActual = TeclasControl.COLORES.first { it.nombre == "Amarillo oscuro" },
            efectoActivo = null,
            onAlternarTema = {}, onEncender = {}, onApagar = {},
            onSubirBrillo = {}, onBajarBrillo = {}, onTecla = {}
        )
    }
}

@Preview(showBackground = true, name = "Efecto activo", heightDp = 800)
@Composable
private fun PreviewEfecto() {
    RGBV10Theme(darkTheme = true, dynamicColor = false) {
        PantallaControlContenido(
            tieneIR = true,
            modoOscuro = true,
            encendida = true,
            teclaActual = TeclasControl.COLORES[2],
            efectoActivo = TeclasControl.EFECTOS.first { it.nombre == "Smooth" },
            onAlternarTema = {}, onEncender = {}, onApagar = {},
            onSubirBrillo = {}, onBajarBrillo = {}, onTecla = {}
        )
    }
}
