package com.dadm.reto10

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import android.view.LayoutInflater
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailForm(ctx: Context, state: OpenDataState, onEvent: (OpenDataEvent)->Unit){
    Scaffold(
        bottomBar = { BottomApp(onEvent = onEvent) }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TxtComponent(txt = state.nombre_pvd)
                Spacer(Modifier.height(10.dp))
                LabelComponent(txt = "Departamento: " + state.depto)
                LabelComponent(txt = "Municipio: " + state.municipio)
                LabelComponent(txt = "Tipo: " + state.tipo_pvd)
                LabelComponent(txt = "Proveedor: " + state.proveedor_conectividad)
                LabelComponent(txt = "Estado: " + state.estado)
                Spacer(Modifier.height(10.dp))

                AndroidView(
                    factory = { context ->
                        val view = LayoutInflater.from(context).inflate(R.layout.custom_map, null, false)
                        val map = view.findViewById<MapView>(R.id.map)

                        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
                        map.setTileSource(TileSourceFactory.MAPNIK)
                        map.setMultiTouchControls(true);
                        map.mapCenter

                        var mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), map)

                        var controller = map.controller

                        mMyLocationOverlay.enableMyLocation()
                        mMyLocationOverlay.enableFollowLocation()
                        mMyLocationOverlay.isDrawAccuracyEnabled = true


                        val mapPoint = GeoPoint(state.longitud.replace(",",".").toDouble(),state.latitud.replace(",",".").toDouble())
                        controller.animateTo(mapPoint)

                        controller.setZoom(18)
                        map.overlays.add(mMyLocationOverlay)

                        val poiMarkers = FolderOverlay(context)
                        val poiMarker = Marker(map)
                        poiMarker.title = "Punto Vive Digital";
                        poiMarker.position = mapPoint
                        poiMarkers.add(poiMarker)

                        map.overlays.add(poiMarkers)

                        view // return the view
                    },
                    update = { view ->
                        /*map.setTileSource(TileSourceFactory.MAPNIK)
                        map.setMultiTouchControls(true);
                        map.mapCenter*/
                    }
                )
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


@Composable
fun LabelComponent(txt: String){
    Text(
        text = txt,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 30.dp),
        style = TextStyle(
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal,
            textAlign = TextAlign.Left
        )
    )
}

@Composable
private fun BottomApp(onEvent: (OpenDataEvent)->Unit) {
    BottomAppBar (
        Modifier.height(50.dp),
        containerColor = Color.LightGray
    ) {
        IconButton(onClick = {
            onEvent(OpenDataEvent.ClearForm)
            onEvent(OpenDataEvent.HideForm)
        }) {
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Volver")
        }
    }
}