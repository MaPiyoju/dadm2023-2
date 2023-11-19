package com.dadm.reto10

import com.google.gson.annotations.SerializedName

data class OpenDataResponse(
    @SerializedName("@odata.context") var context: String,
    @SerializedName("value") var value: List<OpenDataValues>
)

data class OpenDataValues(
    @SerializedName("codigo_pvd") var codigo_pvd: Int,
    @SerializedName("nombre_pvd") var nombre_pvd: String,
    @SerializedName("dane_dpto") var dane_dpto: String,
    @SerializedName("depto") var depto: String,
    @SerializedName("dane_mun") var dane_mun: String,
    @SerializedName("municipio") var municipio: String,
    @SerializedName("tipo_pvd") var tipo_pvd: String,
    @SerializedName("proveedor_conectividad") var proveedor_conectividad: String,
    @SerializedName("estado") var estado: String,
    @SerializedName("longitud") var longitud: String,
    @SerializedName("latitud") var latitud: String
)