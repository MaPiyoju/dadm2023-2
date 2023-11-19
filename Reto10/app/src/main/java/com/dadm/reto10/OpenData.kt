package com.dadm.reto10

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class OpenData(
    val dane_dpto: String,
    val depto: String,
    val dane_mun: String,
    val municipio: String,
    val tipo_pvd: String,
    val proveedor_conectividad: String,
    val nombre_pvd: String,
    val estado: String,
    val longitud: String,
    val latitud: String,

    @PrimaryKey(autoGenerate = true)
    var codigo_pvd: Int = 0
)
