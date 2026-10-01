package com.example.rgbv10.data.infrared

/**
 * Teclas del control típico de 24 botones para tiras LED RGB (NEC, formato IRremote).
 *
 * IMPORTANTE: son los códigos más comunes de ese control, pero varían según el fabricante.
 * Si alguna tecla no responde, hay que capturar el código real de tu control.
 */
data class Tecla(val nombre: String, val codigo: Long, val rojo: Int = 0, val verde: Int = 0, val azul: Int = 0)

object TeclasControl {
    val BRILLO_MAS = Tecla("Brillo +", 0x00F700FF)
    val BRILLO_MENOS = Tecla("Brillo -", 0x00F7807F)
    val APAGAR = Tecla("Apagar", 0x00F740BF)
    val ENCENDER = Tecla("Encender", 0x00F7C03F)

    /** Teclas de color fijo, con su RGB aproximado para elegir la más cercana. */
    val COLORES = listOf(
        Tecla("Rojo", 0x00F720DF, 255, 0, 0),
        Tecla("Verde", 0x00F7A05F, 0, 255, 0),
        Tecla("Azul", 0x00F7609F, 0, 0, 255),
        Tecla("Blanco", 0x00F7E01F, 255, 255, 255),
        Tecla("Naranja rojizo", 0x00F710EF, 255, 70, 0),
        Tecla("Verde claro", 0x00F7906F, 80, 255, 80),
        Tecla("Azul claro", 0x00F750AF, 80, 120, 255),
        Tecla("Naranja", 0x00F730CF, 255, 130, 0),
        Tecla("Cian", 0x00F7B04F, 0, 255, 255),
        Tecla("Violeta", 0x00F7708F, 150, 0, 255),
        Tecla("Amarillo oscuro", 0x00F708F7, 255, 190, 0),
        Tecla("Turquesa", 0x00F78877, 0, 190, 160),
        Tecla("Magenta", 0x00F748B7, 255, 0, 200),
        Tecla("Amarillo", 0x00F728D7, 255, 255, 0),
        Tecla("Azul petróleo", 0x00F7A857, 0, 120, 160),
        Tecla("Rosa", 0x00F76897, 255, 100, 170),
    )

    val EFECTOS = listOf(
        Tecla("Flash", 0x00F7D02F),
        Tecla("Strobe", 0x00F7F00F),
        Tecla("Fade", 0x00F7C837),
        Tecla("Smooth", 0x00F7E817),
    )

    /** Tecla de color más parecida al RGB pedido. */
    fun masCercana(rojo: Int, verde: Int, azul: Int): Tecla = COLORES.minBy {
        val dr = it.rojo - rojo
        val dg = it.verde - verde
        val db = it.azul - azul
        dr * dr + dg * dg + db * db
    }
}
