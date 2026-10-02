package com.example.rgbv10.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.rgbv10.data.ControlLuces
import com.example.rgbv10.data.infrared.TeclasControl

val CLAVE_TECLA = ActionParameters.Key<String>("tecla")

/** Toque en un color: se envía con la misma lógica que la app, sin abrirla. */
class AccionColor : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val tecla = parameters[CLAVE_TECLA]?.let(TeclasControl::porNombre) ?: return
        ControlLuces(context).enviar(tecla)
    }
}

/** Toque en el interruptor: alterna según el último estado conocido. */
class AccionEncendido : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val control = ControlLuces(context)
        if (control.encendida) control.apagar() else control.encender()
        LucesWidget.refrescar(context)
    }
}
