package com.dadm.reto8

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.dadm.reto8.ui.theme.Reto8Theme
import java.util.Timer
import kotlin.concurrent.schedule

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            CompanyDB::class.java,
            "companies.db"
        ).build()
    }

    private val viewModel by viewModels<CompanyViewModel>(
        factoryProducer = {
            object: ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CompanyViewModel(db.dao) as T
                }
            }
        }
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Reto8Theme {
                val state by viewModel._state.collectAsState()
                val onEvent = viewModel::onEvent
                onEvent(CompanyEvent.GetCompanies("",""))
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if(!state.isAddingCompany)
                        List(state = state, onEvent = onEvent)
                    else
                        CompanyForm(ctx = this, state = state, onEvent = onEvent)
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun List(state:CompanyState, onEvent: (CompanyEvent)->Unit, modifier: Modifier = Modifier) {
    val (confirmExit, setConfirmExit) = remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { BottomApp(onEvent) }
    ) {padding->
        if(state.isFilteringCompany){
            FilterElement(state, onEvent)
        }

        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ){
            items(state.companies) { company ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)) {
                    Column(modifier = Modifier.weight(1f).clickable {
                        onEvent(CompanyEvent.SetForm(company))
                        onEvent(CompanyEvent.ShowForm)
                    }) {
                        Text(text = "${company.name}", fontSize = 20.sp)
                        Text(text = "${company.phone} - ${company.email}", fontSize = 12.sp)
                        Text(text = "${company.classification}", fontSize = 12.sp)
                    }
                    IconButton(onClick = {
                        setConfirmExit(true)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar Empresa"
                        )
                    }
                }
                ConfirmAlert(confirmExit, setConfirmExit, onEvent, company)
            }
        }
        if(state.companies.isEmpty()) {
            NoContent()
        }
    }
}

@Composable
private fun NoContent(){
    Row(modifier = Modifier
        .fillMaxSize()
        .padding(40.dp)){
        Text(text = "No existen empresas creadas", fontSize = 30.sp)
    }
}

@Composable
private fun BottomApp(onEvent: (CompanyEvent)->Unit) {
    BottomAppBar (
        Modifier.height(50.dp),
        containerColor = Color.LightGray
    ) {
        IconButton(onClick = {
            onEvent(CompanyEvent.ShowFilter)
        }) {
            Icon(Icons.Filled.Search, contentDescription = "Buscar")
        }
        IconButton(onClick = {
            onEvent(CompanyEvent.SetNameFilter(""))
            onEvent(CompanyEvent.SetClassificationFilter(""))
            Timer().schedule(500) {
                onEvent(CompanyEvent.GetCompanies("", ""))
            }
        }) {
            Icon(Icons.Filled.Refresh, contentDescription = "Limpiar")
        }
        Spacer(Modifier.weight(1f, true))

        IconButton(onClick = {
            onEvent(CompanyEvent.ShowForm)
        }) {
            Icon(imageVector = Icons.Filled.AddCircle, contentDescription = "Nueva empresa")
        }
    }
}

@Composable
fun ConfirmAlert(openDialog: Boolean, setConfirm: (Boolean) -> Unit, onEvent: (CompanyEvent)->Unit, company: Company) {
    MaterialTheme {
        Column {
            if (openDialog) {
                AlertDialog(
                    onDismissRequest = {
                        setConfirm(false)
                    },
                    title = {
                        Text("Confirmación")
                    },
                    text = {
                        Text("Esta seguro de eliminar el elemento?")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onEvent(CompanyEvent.DeleteCompany(company))
                                setConfirm(false)
                                Timer().schedule(500) {
                                    onEvent(CompanyEvent.GetCompanies("", ""))
                                }
                            }) {
                            Text("Si")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                setConfirm(false)
                            }) {
                            Text("No")
                        }
                    }
                )
            }
        }

    }
}