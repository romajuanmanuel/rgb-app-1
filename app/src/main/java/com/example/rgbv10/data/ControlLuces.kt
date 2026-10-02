package com.example.rgbv10.data

import android.content.Context
import com.example.rgbv10.data.infrared.IrController
import com.example.rgbv10.data.infrared.Tecla
import com.example.rgbv10.data.infrared.TeclasControl

/**
 * Única puerta de entrada al control de las luces: envía por IR y guarda el estado
 * (encendido, color y efecto) para que la app y el widget lo compartan, incluso con la app cerrada.
 *
 * El IR es de una sola vía: el estado es lo último que se envió, no lo que informa la tira.
 */
class ControlLuces(context: Context) {

    private val appContext = context.applicationContext
    private val irController = IrController(appContext)
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    val tieneIR: Boolean get() = irController.tieneSoporteIR()

    /** Se asume encendida hasta que se envíe un apagado. */
    val encendida: Boolean get() = prefs.getBoolean(CLAVE_ENCENDIDA, true)

    val colorActual: Tecla?
        get() = prefs.getString(CLAVE_COLOR, null)?.let { nombre ->
            TeclasControl.COLORES.firstOrNull { it.nombre == nombre }
        }

    val efectoActivo: Tecla?
        get() = prefs.getString(CLAVE_EFECTO, null)?.let { nombre ->
            TeclasControl.EFECTOS.firstOrNull { it.nombre == nombre }
        }

    /** Envía una tecla y, si salió bien, actualiza el estado guardado. */
    fun enviar(tecla: Tecla): Boolean {
        if (!irController.enviar(tecla)) return false
        when (tecla) {
            in TeclasControl.COLORES -> prefs.edit()
                .putString(CLAVE_COLOR, tecla.nombre)
                .remove(CLAVE_EFECTO)
                .apply()
            in TeclasControl.EFECTOS -> prefs.edit().putString(CLAVE_EFECTO, tecla.nombre).apply()
        }
        return true
    }

    fun encender(): Boolean = enviarEncendido(TeclasControl.ENCENDER, true)

    fun apagar(): Boolean = enviarEncendido(TeclasControl.APAGAR, false)

    private fun enviarEncendido(tecla: Tecla, encendida: Boolean): Boolean {
        if (!irController.enviar(tecla)) return false
        prefs.edit().putBoolean(CLAVE_ENCENDIDA, encendida).apply()
        return true
    }

    private companion object {
        const val PREFS = "luces_estado"
        const val CLAVE_ENCENDIDA = "encendida"
        const val CLAVE_COLOR = "color"
        const val CLAVE_EFECTO = "efecto"
    }
}
