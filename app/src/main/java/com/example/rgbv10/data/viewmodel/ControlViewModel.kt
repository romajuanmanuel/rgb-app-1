package com.example.rgbv10.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rgbv10.data.ControlLuces
import com.example.rgbv10.data.infrared.Tecla
import com.example.rgbv10.data.infrared.TeclasControl
import com.example.rgbv10.widget.LucesWidget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ControlViewModel(application: Application) : AndroidViewModel(application) {

    // Fuente única del estado de las luces, compartida con el widget
    private val control = ControlLuces(application)
    private val prefs = application.getSharedPreferences("ajustes", Application.MODE_PRIVATE)

    val tieneIR: Boolean = control.tieneIR

    private val _teclaActual = MutableStateFlow(control.colorActual)
    val teclaActual: StateFlow<Tecla?> = _teclaActual

    // Efecto activo (Flash, Strobe, Fade, Smooth); un color fijo lo reemplaza, como en el control físico
    private val _efectoActivo = MutableStateFlow(control.efectoActivo)
    val efectoActivo: StateFlow<Tecla?> = _efectoActivo

    // El IR es de una sola vía: es lo último que se envió, no lo que informa la tira
    private val _encendida = MutableStateFlow(control.encendida)
    val encendida: StateFlow<Boolean> = _encendida

    // Tema oscuro por defecto, con opción de cambiarlo (se recuerda entre sesiones)
    private val _modoOscuro = MutableStateFlow(prefs.getBoolean(CLAVE_OSCURO, true))
    val modoOscuro: StateFlow<Boolean> = _modoOscuro

    fun alternarTema() {
        _modoOscuro.value = !_modoOscuro.value
        prefs.edit().putBoolean(CLAVE_OSCURO, _modoOscuro.value).apply()
    }

    /** Vuelve a leer el estado guardado (el widget pudo haberlo cambiado con la app en segundo plano). */
    fun sincronizar() {
        _teclaActual.value = control.colorActual
        _efectoActivo.value = control.efectoActivo
        _encendida.value = control.encendida
    }

    fun enviarTecla(tecla: Tecla) {
        if (control.enviar(tecla)) sincronizar()
    }

    fun encender() {
        if (control.encender()) cambioDeEncendido()
    }

    fun apagar() {
        if (control.apagar()) cambioDeEncendido()
    }

    private fun cambioDeEncendido() {
        sincronizar()
        viewModelScope.launch { LucesWidget.refrescar(getApplication()) }
    }

    fun subirBrillo() = enviarTecla(TeclasControl.BRILLO_MAS)
    fun bajarBrillo() = enviarTecla(TeclasControl.BRILLO_MENOS)

    private companion object {
        const val CLAVE_OSCURO = "modo_oscuro"
    }
}
