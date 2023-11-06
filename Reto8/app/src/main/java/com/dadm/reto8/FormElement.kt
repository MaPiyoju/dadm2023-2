package com.dadm.reto8

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Timer
import kotlin.concurrent.schedule

@Composable
fun CompanyForm(ctx: Context, state:CompanyState, onEvent: (CompanyEvent)->Unit){
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        CreateForm(ctx, state, onEvent)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateForm(ctx: Context, state:CompanyState, onEvent: (CompanyEvent)->Unit, modifier: Modifier = Modifier) {
    Scaffold(
        bottomBar = { BottomApp(onEvent = onEvent) }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TxtComponent(txt = "Registro de empresa")
                InputComponent(txtValue = state.name, lblValue = "Nombre empresa", Icons.Filled.Create, customEvent = { value: String -> CompanyEvent.SetName(value) }, onEvent=onEvent)
                InputComponent(txtValue = state.url, lblValue = "URL", Icons.Filled.Create, customEvent = { value: String -> CompanyEvent.SetURL(value) }, onEvent=onEvent)
                InputComponent(txtValue = state.phone, lblValue = "Teléfono", Icons.Filled.Call, customEvent = { value: String -> CompanyEvent.SetPhone(value) }, onEvent=onEvent)
                InputComponent(txtValue = state.email, lblValue = "Email", Icons.Filled.Email, customEvent = { value: String -> CompanyEvent.SetEmail(value) }, onEvent=onEvent)
                InputComponent(txtValue = state.products, lblValue = "Productos", Icons.Filled.ShoppingCart, 120, customEvent = { value: String -> CompanyEvent.SetProducts(value) }, onEvent=onEvent)
                Spacer(Modifier.height(10.dp))
                DropDownComponent(ddlValue= state.classification,"Clasificación", Icons.Filled.MoreVert, onEvent=onEvent)
                Spacer(Modifier.height(30.dp))
                BtnComponent(txtValue = if(!state.isEditingCompany) "Registrar" else "Actualizar", onEvent=onEvent)
            }
        }
    }
}

@Composable
fun TxtComponent(txt: String){
    Text(
        text = txt,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            textAlign = TextAlign.Center
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputComponent(txtValue: String, lblValue: String, icon: ImageVector, height: Int = 60, customEvent: (String)->CompanyEvent, onEvent: (CompanyEvent)->Unit){
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp),
        label = { Text(text = lblValue) },
        value = txtValue,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color.Blue,
            focusedLabelColor = Color.Blue,
            cursorColor = Color.Blue
        ),
        keyboardOptions = KeyboardOptions.Default,
        onValueChange = {
            onEvent(customEvent(it))
        },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = "")
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
                        onEvent(CompanyEvent.SetClassification("Consultoría"))
                    }
                )
                DropdownMenuItem(
                    text = { Text("Desarrollo a la medida") },
                    onClick = {
                        isExpanded.value = false
                        onEvent(CompanyEvent.SetClassification("Desarrollo a la medida"))
                    }
                )
                DropdownMenuItem(
                    text = { Text("Fábrica de software") },
                    onClick = {
                        isExpanded.value = false
                        onEvent(CompanyEvent.SetClassification("Fábrica de software"))
                    }
                )
            }
        }
    }
}

@Composable
private fun BtnComponent(txtValue: String, onEvent: (CompanyEvent)->Unit){
    Button(
        onClick = {
            onEvent(CompanyEvent.SaveCompany)
            onEvent(CompanyEvent.HideForm)
            Timer().schedule(500) {
                onEvent(CompanyEvent.GetCompanies("",""))
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(48.dp),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(Color.Transparent)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .heightIn(48.dp)
            .background(
                brush = Brush.horizontalGradient(listOf(Color.Black, Color.DarkGray)),
                shape = RoundedCornerShape(50.dp)
            ),
            contentAlignment = Alignment.Center
        ){
            Text(text = txtValue, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BottomApp(onEvent: (CompanyEvent)->Unit) {
    BottomAppBar (
        Modifier.height(50.dp),
        containerColor = Color.LightGray
    ) {
        IconButton(onClick = {
            onEvent(CompanyEvent.ClearForm)
            onEvent(CompanyEvent.HideForm)
        }) {
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Volver")
        }
    }
}