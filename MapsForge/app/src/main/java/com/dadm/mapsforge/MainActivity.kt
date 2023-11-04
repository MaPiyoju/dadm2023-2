package com.dadm.mapsforge

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.dadm.mapsforge.databinding.ActivityMainBinding
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.InternalRenderTheme
import java.io.FileInputStream
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : AppCompatActivity() {
    
    companion object{
        val COLOMBIA = LatLong(4.637742, -74.084440)

    }

    private lateinit var b: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidGraphicFactory.createInstance(application)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        val contract = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            result?.data?.data?.let{uri->
                openMap(uri)
            }
        }

        b.openMap.setOnClickListener{
            contract.launch(
                Intent(
                    Intent.ACTION_OPEN_DOCUMENT
                ).apply{
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
            )
        }

        runBlocking {
            val tmpURL = read("url")
            if (tmpURL != null) {

            }
        }
    }

    private suspend fun save(key: String, value: String){
        val dataStoreKey = stringPreferencesKey(key)
        dataStore.edit { settings ->
            settings[dataStoreKey] = value
        }
    }

    private suspend fun read(key: String): String?{
        val dataStoreKey = stringPreferencesKey(key)
        val preferences = dataStore.data.first()
        return preferences[dataStoreKey]
    }

    fun openMap(uri: Uri){
        b.map.mapScaleBar.isVisible = true
        b.map.setBuiltInZoomControls(true)
        var cache = AndroidUtil.createTileCache(
            this,
            "mycache",
            b.map.model.displayModel.tileSize,
            1f,
            b.map.model.frameBufferModel.overdrawFactor
        )

        runBlocking {
            save("url", uri.toString())
        }

        val stream = contentResolver.openInputStream(uri) as FileInputStream
        val mapStore = MapFile(stream)
        val renderLayer = TileRendererLayer(
            cache,
            mapStore,
            b.map.model.mapViewPosition,
            AndroidGraphicFactory.INSTANCE
        )

        renderLayer.setXmlRenderTheme(
            InternalRenderTheme.DEFAULT
        )

        b.map.layerManager.layers.add(renderLayer)
        b.map.setCenter(COLOMBIA)
        b.map.setZoomLevel(15)


    }
}