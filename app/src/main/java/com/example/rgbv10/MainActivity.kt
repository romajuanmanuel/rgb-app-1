package com.example.rgbv10

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rgbv10.data.infrared.TeclasControl
import com.example.rgbv10.data.viewmodel.ControlViewModel
import com.example.rgbv10.ui.theme.RGBV10Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ControlViewModel = viewModel()
            val modoOscuro by viewModel.modoOscuro.collectAsState()

            RGBV10Theme(darkTheme = modoOscuro) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaControl(
                        modoOscuro = modoOscuro,
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel
                    )
                }
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
    val temaClaro = MaterialTheme.colorScheme.background.luminance() > 0.5f

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Estado de la luz actual + cambio de tema
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            val colorActual = teclaActual?.let { Color(it.rojo, it.verde, it.azul) }
            val esBlancoActual = teclaActual?.let { it.rojo == 255 && it.verde == 255 && it.azul == 255 } == true
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colorActual ?: MaterialTheme.colorScheme.surfaceVariant)
                    .then(
                        if (esBlancoActual && temaClaro) Modifier.border(2.dp, Color.Black, CircleShape)
                        else Modifier
                    )
            )
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text("Color actual", style = MaterialTheme.typography.labelMedium)
                Text(teclaActual?.nombre ?: "Sin seleccionar", style = MaterialTheme.typography.titleMedium)
            }
            OutlinedButton(onClick = viewModel::alternarTema) {
                Text(if (modoOscuro) "Tema claro" else "Tema oscuro")
            }
        }

        if (!viewModel.tieneIR) {
            Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
                Text(
                    "Este celular no tiene emisor infrarrojo: los comandos no se van a enviar.",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = viewModel::encender, modifier = Modifier.weight(1f)) { Text("Encender") }
            OutlinedButton(onClick = viewModel::apagar, modifier = Modifier.weight(1f)) { Text("Apagar") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = viewModel::bajarBrillo, modifier = Modifier.weight(1f)) { Text("Brillo −") }
            OutlinedButton(onClick = viewModel::subirBrillo, modifier = Modifier.weight(1f)) { Text("Brillo +") }
        }

        Text("Colores", style = MaterialTheme.typography.titleMedium)
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(TeclasControl.COLORES) { tecla ->
                val seleccionada = tecla == teclaActual
                val esBlanco = tecla.rojo == 255 && tecla.verde == 255 && tecla.azul == 255
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(Color(tecla.rojo, tecla.verde, tecla.azul))
                        .then(
                            when {
                                seleccionada -> Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                // En tema claro el blanco se pierde con el fondo: borde negro
                                esBlanco && temaClaro -> Modifier.border(2.dp, Color.Black, CircleShape)
                                else -> Modifier
                            }
                        )
                        .clickable { viewModel.enviarTecla(tecla) }
                )
            }
        }

        Text("Efectos", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TeclasControl.EFECTOS.forEach { efecto ->
                OutlinedButton(
                    onClick = { viewModel.enviarTecla(efecto) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) { Text(efecto.nombre) }
            }
        }
    }
}
