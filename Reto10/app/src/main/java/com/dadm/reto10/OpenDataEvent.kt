package com.dadm.reto10

sealed interface OpenDataEvent {

    data class SetNameFilter(val nameFilter: String): OpenDataEvent
    data class SetMunicipioFilter(val municipioFilter: String): OpenDataEvent
    data class SetTipoFilter(val tipoFilter: String): OpenDataEvent
    data class SetEstadoFilter(val estadoFilter: String): OpenDataEvent

    object ShowForm: OpenDataEvent
    object HideForm: OpenDataEvent

    object ShowFilter: OpenDataEvent
    object HideFilter: OpenDataEvent

    data class GetData(val query: String): OpenDataEvent

    data class SetForm(val openData: OpenDataValues): OpenDataEvent
    object ClearForm: OpenDataEvent
}
