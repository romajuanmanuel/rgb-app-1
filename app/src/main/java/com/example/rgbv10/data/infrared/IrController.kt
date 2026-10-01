package com.example.rgbv10.data.infrared

import android.content.Context
import android.hardware.ConsumerIrManager
import android.util.Log

/**
 * Envía comandos infrarrojos NEC a las luces RGB usando el emisor IR del celular.
 */
class IrController(context: Context) {

    private val irManager: ConsumerIrManager? =
        context.getSystemService(ConsumerIrManager::class.java)

    fun tieneSoporteIR(): Boolean = irManager?.hasIrEmitter() == true

    /** Envía una tecla del control. Devuelve false si no se pudo transmitir. */
    fun enviar(tecla: Tecla): Boolean {
        val manager = irManager
        if (manager == null || !manager.hasIrEmitter()) {
            Log.w(TAG, "Este dispositivo no tiene transmisor IR")
            return false
        }
        return try {
            manager.transmit(NecEncoder.FRECUENCIA_HZ, NecEncoder.codificar(tecla.codigo))
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar IR: ${e.message}", e)
            false
        }
    }

    /** Estas luces solo tienen colores fijos: se envía el más cercano al RGB pedido. */
    fun enviarColor(rojo: Int, verde: Int, azul: Int): Tecla {
        val tecla = TeclasControl.masCercana(
            rojo.coerceIn(0, 255), verde.coerceIn(0, 255), azul.coerceIn(0, 255)
        )
        enviar(tecla)
        return tecla
    }

    private companion object {
        const val TAG = "IrController"
    }
}
