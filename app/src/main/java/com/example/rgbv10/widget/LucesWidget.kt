package com.example.rgbv10.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.rgbv10.R
import com.example.rgbv10.data.ControlLuces
import com.example.rgbv10.data.infrared.TeclasControl

/**
 * Widget de acceso rápido: Rosa, Cian, Naranja rojizo y encendido.
 * No tiene lógica de luces propia: delega todo en [ControlLuces].
 */
class LucesWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { Contenido() }
    }

    companion object {
        /** Copia del estado de encendido para que Glance sepa cuándo redibujar. */
        val CLAVE_ENCENDIDA = booleanPreferencesKey("encendida")

        /** Redibuja todos los widgets con el estado actual de [ControlLuces]. */
        suspend fun refrescar(context: Context) {
            val encendida = ControlLuces(context).encendida
            GlanceAppWidgetManager(context).getGlanceIds(LucesWidget::class.java).forEach { id ->
                updateAppWidgetState(context, id) { it[CLAVE_ENCENDIDA] = encendida }
                LucesWidget().update(context, id)
            }
        }
    }
}

private val RojoNeon = Color(0xFFFF1744)
private val TextoSuave = Color(0xFF7F8AB8)

@Composable
private fun Contenido() {
    val context = LocalContext.current
    val encendida = currentState<Preferences>()[LucesWidget.CLAVE_ENCENDIDA]
        ?: ControlLuces(context).encendida

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_fondo))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TeclasControl.ACCESO_RAPIDO.forEach { tecla ->
            Box(
                modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(52.dp)
                        .background(ImageProvider(R.drawable.widget_tecla_fondo))
                        .clickable(
                            actionRunCallback<AccionColor>(actionParametersOf(CLAVE_TECLA to tecla.nombre))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.widget_circulo),
                        contentDescription = tecla.nombre,
                        colorFilter = ColorFilter.tint(ColorProvider(Color(tecla.rojo, tecla.verde, tecla.azul))),
                        modifier = GlanceModifier.size(34.dp)
                    )
                }
            }
        }

        // Interruptor: un toque alterna; se resalta el estado actual
        Box(
            modifier = GlanceModifier
                .width(78.dp)
                .height(52.dp)
                .background(ImageProvider(R.drawable.widget_power_fondo))
                .clickable(actionRunCallback<AccionEncendido>()),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextoEstado("OFF", activo = !encendida)
                Spacer(modifier = GlanceModifier.width(6.dp))
                TextoEstado("ON", activo = encendida)
            }
        }
    }
}

@Composable
private fun TextoEstado(texto: String, activo: Boolean) {
    Text(
        text = texto,
        style = TextStyle(
            color = ColorProvider(if (activo) RojoNeon else TextoSuave),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    )
}
