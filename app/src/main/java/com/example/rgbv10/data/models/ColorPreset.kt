package com.example.rgbv10.data.models

// Data class para representar un preset de color
data class ColorPreset(
    val id: Int = 0,
    val nombre: String = "",
    val rojo: Int = 0,
    val verde: Int = 0,
    val azul: Int = 0,
    val codigoIR: String = ""  // El comando IR específico
)