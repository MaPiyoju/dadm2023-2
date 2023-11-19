package com.dadm.reto10

import android.annotation.SuppressLint
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.dadm.reto10.ui.theme.Reto10Theme
import java.util.Timer
import kotlin.concurrent.schedule

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<OpenDataViewModel>(
        factoryProducer = {
            object: ViewModelProvider.Factory{
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return OpenDataViewModel() as T
                }
            }
        }
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Reto10Theme {
                val state by viewModel._state.collectAsState()
                val onEvent = viewModel::onEvent
                onEvent(OpenDataEvent.GetData(""))
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if(!state.isDetailData)
                        List(state = state, onEvent = onEvent)
                    else
                        DetailForm(ctx = this, state = state, onEvent = onEvent)
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun List(state: OpenDataState, onEvent: (OpenDataEvent)->Unit, modifier: Modifier = Modifier) {
    val (confirmExit, setConfirmExit) = remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { BottomApp(onEvent) }
    ) {padding->
        if(state.isFilteringData && !state.isDetailData){
            FilterElement(state, onEvent)
        }

        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ){
            items(state.openData) { data ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)) {
                    Column(modifier = Modifier.weight(1f).clickable {
                        onEvent(OpenDataEvent.SetForm(data))
                        onEvent(OpenDataEvent.ShowForm)
                    }) {
                        Text(text = "${data.nombre_pvd}", fontSize = 20.sp)
                        Text(text = "${data.depto} - ${data.municipio}", fontSize = 12.sp)
                        Text(text = "${data.estado}", fontSize = 12.sp)
                    }
                }
            }
        }
        if(state.openData.isEmpty()) {
            NoContent()
        }
    }
}

@Composable
private fun NoContent(){
    Row(modifier = Modifier
        .fillMaxSize()
        .padding(40.dp)){
        Text(text = "No hay registros para mostrar", fontSize = 30.sp)
    }
}

@Composable
private fun BottomApp(onEvent: (OpenDataEvent)->Unit) {
    BottomAppBar (
        Modifier.height(50.dp),
        containerColor = Color.LightGray
    ) {
        IconButton(onClick = {
            onEvent(OpenDataEvent.ShowFilter)
        }) {
            Icon(Icons.Filled.Search, contentDescription = "Buscar")
        }
        IconButton(onClick = {
            onEvent(OpenDataEvent.SetNameFilter(""))
            onEvent(OpenDataEvent.SetMunicipioFilter(""))
            onEvent(OpenDataEvent.SetEstadoFilter(""))
            onEvent(OpenDataEvent.SetTipoFilter(""))
            Timer().schedule(500) {
                onEvent(OpenDataEvent.GetData(""))
            }
        }) {
            Icon(Icons.Filled.Refresh, contentDescription = "Limpiar")
        }
        Spacer(Modifier.weight(1f, true))
    }
}