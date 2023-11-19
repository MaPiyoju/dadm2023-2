package com.dadm.reto10

data class OpenDataState(
    val openData: List<OpenDataValues> = emptyList(),

    val municipioFilterList: List<String> = emptyList(),
    val tipoFilterList: List<String> = emptyList(),
    val estadoFilterList: List<String> = emptyList(),

    val codigo_pvd: String = "",
    val dane_dpto: String = "",
    val depto: String = "",
    val dane_mun: String = "",
    val municipio: String = "",
    val tipo_pvd: String = "",
    val proveedor_conectividad: String = "",
    val nombre_pvd: String = "",
    val estado: String = "",
    val longitud: String = "",
    val latitud: String = "",

    val isDetailData: Boolean = false,
    val isFilteringData: Boolean = false,
    val loadedFilters: Boolean = false,

    val nameFilter: String = "",
    val municipioFilter: String = "",
    val tipoFilter: String = "",
    val estadoFilter: String = ""
)
