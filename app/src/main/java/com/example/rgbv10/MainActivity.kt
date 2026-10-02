package com.example.rgbv10

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ControlViewModel = viewModel()
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

    PantallaControlContenido(
        tieneIR = viewModel.tieneIR,
        modoOscuro = modoOscuro,
        encendida = encendida,
        teclaActual = teclaActual,
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
            EstadoActual(c, teclaActual, onAlternarTema)
            EstadoIR(c, tieneIR)
            PanelMando(
                c = c,
                encendida = encendida,
                teclaActual = teclaActual,
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
private fun EstadoActual(c: CyberColores, tecla: Tecla?, onAlternarTema: () -> Unit) {
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
        Box(
            modifier = Modifier
                .size(44.dp)
                .drawBehind { if (color != null) glowCirculo(color.neon(), c.glow, 8.dp.toPx()) }
                .clip(CircleShape)
                .background(color ?: c.tecla)
                .border(2.dp, borde, CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
            EtiquetaTecnica(c, "// COLOR ACTUAL")
            Text(
                text = (tecla?.nombre ?: "SIN SELECCIONAR").uppercase(),
                color = c.texto,
                fontFamily = FuenteTecnica,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 1.sp
            )
        }
        CyberChip(c, if (c.oscuro) "TEMA CLARO" else "TEMA OSCURO", onAlternarTema)
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
    onEncender: () -> Unit,
    onApagar: () -> Unit,
    onSubirBrillo: () -> Unit,
    onBajarBrillo: () -> Unit,
    onTecla: (Tecla) -> Unit
) {
    val colores = TeclasControl.COLORES
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
            colores.subList(0, 4).forEach { TeclaColor(c, it, it == teclaActual, onTecla, Modifier.weight(1f).then(celda)) }
        }

        TeclasControl.EFECTOS.forEachIndexed { i, efecto ->
            FilaMando {
                colores.subList(4 + i * 3, 7 + i * 3).forEach {
                    TeclaColor(c, it, it == teclaActual, onTecla, Modifier.weight(1f).then(celda))
                }
                CyberTecla(
                    c = c, neon = c.magenta, onClick = { onTecla(efecto) }, descripcion = efecto.nombre,
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
            onAlternarTema = {}, onEncender = {}, onApagar = {},
            onSubirBrillo = {}, onBajarBrillo = {}, onTecla = {}
        )
    }
}
