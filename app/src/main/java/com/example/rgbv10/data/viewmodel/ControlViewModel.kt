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

    val tieneIR: Boolean = irController.tieneSoporteIR()

    private val _teclaActual = MutableStateFlow<Tecla?>(null)
    val teclaActual: StateFlow<Tecla?> = _teclaActual

    fun enviarTecla(tecla: Tecla) {
        if (irController.enviar(tecla) && tecla in TeclasControl.COLORES) {
            _teclaActual.value = tecla
        }
    }

    fun enviarColor(rojo: Int, verde: Int, azul: Int) {
        _teclaActual.value = irController.enviarColor(rojo, verde, azul)
    }

    fun encender() = enviarTecla(TeclasControl.ENCENDER)
    fun apagar() = enviarTecla(TeclasControl.APAGAR)
    fun subirBrillo() = enviarTecla(TeclasControl.BRILLO_MAS)
    fun bajarBrillo() = enviarTecla(TeclasControl.BRILLO_MENOS)
}
