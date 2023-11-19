package com.dadm.reto10

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OpenDataViewModel (): ViewModel(){

    val _state = MutableStateFlow(OpenDataState())

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://www.datos.gov.co/api/odata/v4/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun onEvent(event: OpenDataEvent){
        when(event) {
            OpenDataEvent.HideForm -> {
                _state.update {
                    it.copy(
                        isDetailData = false,
                        isFilteringData = false
                    )
                }
            }

            OpenDataEvent.ShowForm -> {
                _state.update {
                    it.copy(
                        isDetailData = true
                    )
                }
            }

            is OpenDataEvent.GetData -> {
                viewModelScope.launch {
                    val call = getRetrofit().create(APIService::class.java).getData("etr2-mkeu?"+event.query)
                    val responseData = call.body()
                    if(call.isSuccessful){
                        var openDataItems = responseData?.value?: emptyList()
                        if (_state.value.nameFilter != "")
                            openDataItems = openDataItems.filter { it.nombre_pvd.contains(_state.value.nameFilter, ignoreCase = true) }

                        _state.update {
                            it.copy(
                                openData = openDataItems
                            )
                        }
                        if(!_state.value.loadedFilters){
                            val municipiosTmp = _state.value.openData.map { it.municipio }.distinct()
                            val tipoTmp = _state.value.openData.map { it.tipo_pvd }.distinct()
                            val estadoTmp = _state.value.openData.map { it.estado }.distinct()

                            _state.update {
                                it.copy(
                                    municipioFilterList = municipiosTmp,
                                    tipoFilterList = tipoTmp,
                                    estadoFilterList = estadoTmp,
                                    loadedFilters = true
                                )
                            }
                        }
                    }else{

                    }
                }
            }

            OpenDataEvent.HideFilter -> {
                _state.update {
                    it.copy(
                        isFilteringData = false
                    )
                }
            }

            OpenDataEvent.ShowFilter -> {
                _state.update {
                    it.copy(
                        isFilteringData = true
                    )
                }
            }

            is OpenDataEvent.SetNameFilter -> {
                _state.update {
                    it.copy(
                        nameFilter = event.nameFilter
                    )
                }
            }

            is OpenDataEvent.SetMunicipioFilter -> {
                _state.update {
                    it.copy(
                        municipioFilter = event.municipioFilter
                    )
                }
            }

            is OpenDataEvent.SetTipoFilter -> {
                _state.update {
                    it.copy(
                        tipoFilter = event.tipoFilter
                    )
                }
            }

            is OpenDataEvent.SetEstadoFilter -> {
                _state.update {
                    it.copy(
                        estadoFilter = event.estadoFilter
                    )
                }
            }

            is OpenDataEvent.SetForm -> {
                _state.update {
                    it.copy(
                        dane_dpto = event.openData.dane_dpto,
                        depto = event.openData.depto,
                        dane_mun = event.openData.dane_mun,
                        municipio = event.openData.municipio,
                        tipo_pvd = event.openData.tipo_pvd,
                        proveedor_conectividad = event.openData.proveedor_conectividad,
                        nombre_pvd = event.openData.nombre_pvd,
                        estado = event.openData.estado,
                        longitud = event.openData.longitud,
                        latitud = event.openData.latitud
                    )
                }
            }

            OpenDataEvent.ClearForm -> {
                _state.update {
                    it.copy(
                        codigo_pvd = "",
                        dane_dpto = "",
                        depto = "",
                        dane_mun = "",
                        municipio = "",
                        tipo_pvd = "",
                        proveedor_conectividad = "",
                        nombre_pvd = "",
                        estado = "",
                        longitud = "",
                        latitud = "",
                        isDetailData = false
                    )
                }
            }
        }
    }

}