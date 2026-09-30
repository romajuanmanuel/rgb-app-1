package com.example.rgbv10.data.infrared

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Controlador para enviar comandos infrarrojos
 * Maneja el envío de códigos IR para controlar luces RGB
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)  // ConsumerIrManager está disponible desde API 19
class IrController(private val context: Context) {

    private val irManager: ConsumerIrManager? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            context.getSystemService(ConsumerIrManager::class.java)
        } else {
            null
        }
    }

    /**
     * Verifica si el dispositivo tiene transmisor IR
     */
    fun tieneSoporteIR(): Boolean {
        return irManager != null && irManager?.hasIrEmitter() == true
    }

    /**
     * Envía un comando IR genérico (formato NEC estándar)
     * @param frecuencia Frecuencia en Hz (típicamente 38000 para RGB)
     * @param patron Array de tiempos en microsegundos
     */
    fun enviarComando(frecuencia: Int = 38000, patron: IntArray) {
        if (!tieneSoporteIR()) {
            println("❌ Este dispositivo no tiene transmisor IR")
            return
        }

        try {
            irManager?.transmit(frecuencia, patron)
            println("✅ Comando IR enviado")
        } catch (e: Exception) {
            println("❌ Error al enviar IR: ${e.message}")
        }
    }

    /**
     * Envía un color RGB usando protocolo NEC estándar
     *
     * IMPORTANTE: Los códigos aquí son EJEMPLOS
     * Debes reemplazarlos con los códigos reales de tus luces
     */
    fun enviarColor(rojo: Int, verde: Int, azul: Int) {
        // Validar rangos
        val r = rojo.coerceIn(0, 255)
        val g = verde.coerceIn(0, 255)
        val b = azul.coerceIn(0, 255)

        // Convertir a hexadecimal
        val hexColor = String.format("%02X%02X%02X", r, g, b)
        println("📤 Enviando color RGB: ($r, $g, $b) → #$hexColor")

        // Aquí irían los códigos IR reales
        // Por ahora, es un placeholder
        val patron = generarPatronNEC(hexColor)
        enviarComando(38000, patron)
    }

    /**
     * Envía controles de brillo (0-100%)
     */
    fun establecerBrillo(porcentaje: Int) {
        val brillo = porcentaje.coerceIn(0, 100)
        println("🔆 Ajustando brillo a: $brillo%")

        // Placeholder - reemplazar con código IR real
        // Ejemplo simple: si es > 50%, enviar comando de brillo alto
        val patron = if (brillo > 50) {
            generarPatronNEC("BRIGHT_HIGH")
        } else {
            generarPatronNEC("BRIGHT_LOW")
        }
        enviarComando(38000, patron)
    }

    /**
     * Envía comando de encendido/apagado
     */
    fun alternarEncendido() {
        println("🔘 Toggle Encendido/Apagado")
        val patron = generarPatronNEC("POWER_TOGGLE")
        enviarComando(38000, patron)
    }

    /**
     * Genera un patrón IR básico (PLACEHOLDER)
     *
     * En producción, necesitarás los códigos reales de tu control remoto.
     * Puedes obtenerlos:
     * 1. Del manual de tu LED RGB
     * 2. Decodificando tu control remoto con un analizador IR
     * 3. Descargándolos de bases de datos IR online
     */
    private fun generarPatronNEC(comando: String): IntArray {
        // Este es un patrón NEC EJEMPLO
        // Formato: [lider_on, lider_off, bit0_on, bit0_off, bit1_on, bit1_off, ...]

        // Patrón de prueba simple (no es un comando real)
        return intArrayOf(
            9000,   // Lider ON (9ms)
            4500,   // Lider OFF (4.5ms)
            560, 560,   // Bit 1
            560, 1690,  // Bit 0
            560, 560,   // Bit 1
            560, 1690,  // Bit 0
            560, 560,   // ... continuar según sea necesario
            560       // Final
        )
    }
}