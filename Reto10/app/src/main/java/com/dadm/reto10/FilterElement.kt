package com.dadm.reto10

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterElement(state: OpenDataState, onEvent: (OpenDataEvent) -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            onEvent(OpenDataEvent.GetData(""))
            onEvent(OpenDataEvent.HideFilter)
        },
        title = { Text(text = "Filtrar")},
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){
                TextField(
                    value = state.nameFilter,
                    onValueChange = {
                        onEvent(OpenDataEvent.SetNameFilter(it))
                    },
                    placeholder = {
                        Text(text = "Nombre PVD")
                    }
                )
                DropDownComponent(ddlValue= state.municipioFilter,"Municipio", Icons.Filled.MoreVert, state.municipioFilterList,customEvent = { value: String -> OpenDataEvent.SetMunicipioFilter(value) }, onEvent=onEvent)
            }
        },
        confirmButton = { 
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ){
                Button(
                    onClick = {
                        var query = ""
                        val queryTerms: ArrayList<String> = arrayListOf()

                        if (state.municipioFilter != "")
                            queryTerms.add("municipio eq '"+state.municipioFilter+"'")
                        if (state.tipoFilter != "")
                            queryTerms.add("tipo_pvd eq '"+state.tipoFilter+"'")
                        if (state.estadoFilter != "")
                            queryTerms.add("estado eq '"+state.estadoFilter+"'")

                        val tmpQuery = queryTerms.joinToString(prefix = "\$filter=", separator = " and ")

                        if(tmpQuery.isNotEmpty())
                            query = tmpQuery

                        onEvent(
                            OpenDataEvent.GetData(query)
                        )
                        onEvent(OpenDataEvent.HideFilter)
                    }
                ) {
                    Text(text = "Aplicar filtro")
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropDownComponent(ddlValue: String, lblValue: String, icon: ImageVector, items: List<String>, customEvent: (String)->OpenDataEvent, onEvent: (OpenDataEvent)->Unit){
    val isExpanded = remember {
        mutableStateOf(false)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        ExposedDropdownMenuBox(
            expanded = isExpanded.value,
            onExpandedChange = { isExpanded.value = it },
        ) {
            TextField(
                label = { Text(text = lblValue) },
                leadingIcon = {
                    Icon(imageVector = icon, contentDescription = "")
                },
                value = ddlValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded.value)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = isExpanded.value,
                onDismissRequest = { isExpanded.value = false }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item) },
                        onClick = {
                            isExpanded.value = false
                            onEvent(customEvent(item))
                        }
                    )
                }
            }
        }
    }
}