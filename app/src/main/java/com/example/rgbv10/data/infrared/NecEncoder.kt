package com.example.rgbv10.data.infrared

/**
 * Convierte un código NEC de 32 bits en el patrón de pulsos que espera ConsumerIrManager.
 *
 * El código se interpreta como en IRremote: el bit 31 es el primer bit que sale por el LED.
 * Portadora: 38 kHz.
 */
object NecEncoder {
    const val FRECUENCIA_HZ = 38000

    private const val LIDER_ON = 9000
    private const val LIDER_OFF = 4500
    private const val MARCA = 560
    private const val ESPACIO_0 = 560
    private const val ESPACIO_1 = 1690

    fun codificar(codigo: Long): IntArray {
        val patron = IntArray(2 + 32 * 2 + 1)
        patron[0] = LIDER_ON
        patron[1] = LIDER_OFF
        for (i in 0 until 32) {
            val bit = (codigo shr (31 - i)) and 1L
            patron[2 + i * 2] = MARCA
            patron[3 + i * 2] = if (bit == 1L) ESPACIO_1 else ESPACIO_0
        }
        patron[patron.size - 1] = MARCA
        return patron
    }
}
