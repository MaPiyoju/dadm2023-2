package com.dadm.reto8

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
fun FilterElement(state: CompanyState, onEvent: (CompanyEvent) -> Unit, modifier: Modifier = Modifier) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            onEvent(CompanyEvent.GetCompanies("", ""))
            onEvent(CompanyEvent.HideFilter)
        },
        title = { Text(text = "Filtrar")},
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){
                TextField(
                    value = state.nameFilter,
                    onValueChange = {
                        onEvent(CompanyEvent.SetNameFilter(it))
                    },
                    placeholder = {
                        Text(text = "Filtro de nombre")
                    }
                )
                DropDownComponent(ddlValue= state.classificationFilter,"Filtro de Clasificación", Icons.Filled.MoreVert, onEvent=onEvent)
            }
        },
        confirmButton = { 
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ){
                Button(
                    onClick = {
                        onEvent(CompanyEvent.GetCompanies(state.nameFilter, state.classificationFilter))
                        onEvent(CompanyEvent.HideFilter)
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
private fun DropDownComponent(ddlValue: String, lblValue: String, icon: ImageVector, onEvent: (CompanyEvent)->Unit){
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
                DropdownMenuItem(
                    text = { Text("Consultoría") },
                    onClick = {
                        isExpanded.value = false
                        onEvent(CompanyEvent.SetClassificationFilter("Consultoría"))
                    }
                )
                DropdownMenuItem(
                    text = { Text("Desarrollo a la medida") },
                    onClick = {
                        isExpanded.value = false
                        onEvent(CompanyEvent.SetClassificationFilter("Desarrollo a la medida"))
                    }
                )
                DropdownMenuItem(
                    text = { Text("Fábrica de software") },
                    onClick = {
                        isExpanded.value = false
                        onEvent(CompanyEvent.SetClassificationFilter("Fábrica de software"))
                    }
                )
            }
        }
    }
}