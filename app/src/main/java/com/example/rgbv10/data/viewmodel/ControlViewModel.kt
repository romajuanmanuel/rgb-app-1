package com.example.rgbv10.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rgbv10.data.infrared.IrController
import com.example.rgbv10.data.infrared.Tecla
import com.example.rgbv10.data.infrared.TeclasControl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ControlViewModel(application: Application) : AndroidViewModel(application) {

    private val irController = IrController(application)

    private val prefs = application.getSharedPreferences("ajustes", Application.MODE_PRIVATE)

    val tieneIR: Boolean = irController.tieneSoporteIR()

    // Tema oscuro por defecto; se recuerda la elección entre sesiones
    private val _modoOscuro = MutableStateFlow(prefs.getBoolean(CLAVE_OSCURO, true))
    val modoOscuro: StateFlow<Boolean> = _modoOscuro

    fun alternarTema() {
        _modoOscuro.value = !_modoOscuro.value
        prefs.edit().putBoolean(CLAVE_OSCURO, _modoOscuro.value).apply()
    }

    private val _teclaActual = MutableStateFlow<Tecla?>(null)
    val teclaActual: StateFlow<Tecla?> = _teclaActual

    // Efecto activo (Flash, Strobe, Fade, Smooth); un color fijo lo reemplaza, como en el control físico
    private val _efectoActivo = MutableStateFlow<Tecla?>(null)
    val efectoActivo: StateFlow<Tecla?> = _efectoActivo

    fun enviarTecla(tecla: Tecla) {
        if (!irController.enviar(tecla)) return
        when (tecla) {
            in TeclasControl.COLORES -> {
                _teclaActual.value = tecla
                _efectoActivo.value = null
            }
            in TeclasControl.EFECTOS -> _efectoActivo.value = tecla
        }
    }

    fun enviarColor(rojo: Int, verde: Int, azul: Int) {
        _teclaActual.value = irController.enviarColor(rojo, verde, azul)
    }

    // El IR es de una sola vía: se asume encendida al abrir y se actualiza con lo que envía la app
    private val _encendida = MutableStateFlow(true)
    val encendida: StateFlow<Boolean> = _encendida

    fun encender() {
        if (irController.enviar(TeclasControl.ENCENDER)) _encendida.value = true
    }

    fun apagar() {
        if (irController.enviar(TeclasControl.APAGAR)) _encendida.value = false
    }
    fun subirBrillo() = enviarTecla(TeclasControl.BRILLO_MAS)
    fun bajarBrillo() = enviarTecla(TeclasControl.BRILLO_MENOS)

    private companion object {
        const val CLAVE_OSCURO = "modo_oscuro"
    }
}
