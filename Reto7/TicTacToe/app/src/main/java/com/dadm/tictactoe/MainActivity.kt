package com.dadm.tictactoe

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.dadm.tictactoe.ui.theme.TicTacToeTheme
import io.socket.client.Socket
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SocketHandler.setSocket()
        SocketHandler.establishConnection()

        val mSocket = SocketHandler.getSocket()
        var rooms = arrayOf<String>()
        var session = ""

        val intent = this.intent
        val sessionId = intent.getStringExtra("SESSION")
        if (sessionId != null) {
            session = sessionId
        }

        mSocket.on("session") { args ->
            if (args[0] != null) {
                val jsonObject = JSONObject(args[0].toString())

                session = jsonObject.getString("sessionID")

                val tmpRooms = jsonObject.getJSONArray("rooms")
                rooms = Array(tmpRooms.length()) {
                    tmpRooms.getJSONObject(it).getString("room")
                }
            }
        }

        setContent {
            TicTacToeTheme {
                MatchGrid(this@MainActivity, this, mSocket, rooms, session)
            }
        }
    }
}



@Composable
private fun BtnHolder(
    txt: String, modifier: Modifier, orientationMode: MutableState<Int>, ctx:Context, action: (Any?) -> Unit
) {
    val courierFamily = FontFamily(
        Font(R.font.courier_regular, FontWeight.Normal),
        Font(R.font.courier_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.courier_bold, FontWeight.Bold)
    )
    Surface(
        tonalElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth(),
        color = Color(50,50,50),
        contentColor = Color.White,
    ) {
        Button( onClick = { action(ctx) }) {
            Text(txt)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Rooms(
    txt: String, modifier: Modifier, orientationMode: MutableState<Int>, clickEvent : () -> Unit
) {
    val courierFamily = FontFamily(
        Font(R.font.courier_regular, FontWeight.Normal),
        Font(R.font.courier_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.courier_bold, FontWeight.Bold)
    )
    Card(modifier = Modifier.padding(8.dp), onClick = clickEvent) {
        Column {
            Text(
                text = txt,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

fun loadSingle(ctx:Context) {
    ctx.startActivity(Intent(ctx, SinglePlayer::class.java))
}

fun loadMulti(ctx:Context, room: String, sessionId: String) {
    val intent = Intent(ctx, MultiPlayer::class.java)
    intent.putExtra("ROOM", room)
    intent.putExtra("SESSION", sessionId)
    ctx.startActivity(intent)
}

@Composable
fun MatchGrid(context: Activity, ctx:Context, mSocket: Socket, initRooms: Array<String>, session: String, modifier: Modifier = Modifier) {
    var (isSearching, setIsSearching) = rememberSaveable { mutableStateOf(false) }
    val rooms = rememberSaveable { mutableStateOf(initRooms) }

    val config = LocalConfiguration.current
    var orientationMode = remember { mutableStateOf(config.orientation) }

    mSocket.on("load_rooms") { args ->
        if (args[0] != null) {
            val jsonObject = JSONObject(args[0].toString())
            val tmpRooms = jsonObject.getJSONArray("rooms")
            rooms.value = Array(tmpRooms.length()) {
                tmpRooms.getJSONObject(it).getString("room")
            }
        }
    }

    mSocket.on("match_found") { args ->
        if (args[0] != null) {
            loadMulti(context, args[0].toString(), session)
        }
    }


    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    if(isSearching){
        Column() {
            Image(
                painter = rememberAsyncImagePainter(R.drawable.enemyspinner, imageLoader),
                contentDescription = "Loading!!",
                modifier = Modifier
                    .fillMaxSize(0.9f)
                    .align(CenterHorizontally)
                    .scale(0.6F),
            )
            BtnHolder(
                "Cancelar búsqueda", modifier, orientationMode, context
            ) {
                mSocket.emit("leave_room",session)
                setIsSearching(false)
            }
        }
    }else {
        if (orientationMode.value == Configuration.ORIENTATION_PORTRAIT) {
            Column() {
                LazyColumn {
                    items(rooms.value) { item ->
                        Rooms(item, modifier, orientationMode) {
                            mSocket.emit("join_room", JSONObject("{ room: $item, user: $session}"))
                        }
                    }
                }
                Column(modifier = Modifier.padding(vertical = 5.dp)) {
                    BtnHolder(
                        "Nueva sala multijugador", modifier, orientationMode, context
                    ) {
                        mSocket.emit("create_room", session)
                        setIsSearching(true)
                    }
                    BtnHolder(
                        "Jugar Vs. Máquina", modifier, orientationMode, context
                    ) { loadSingle(context) }
                }

            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LazyColumn {
                    items(rooms.value) { item ->
                        Rooms(item, modifier, orientationMode) {
                            mSocket.emit("join_room", JSONObject("{ room: $item, user2: $session}"))
                            Log.i("TEST", "dafuc")
                        }
                    }
                }
                Column() {
                    BtnHolder(
                        "Nueva sala multijugador", modifier, orientationMode, context
                    ) {
                        mSocket.emit("create_room", session)
                        setIsSearching(true)
                    }
                    BtnHolder(
                        "Jugar Vs. Máquina", modifier, orientationMode, context
                    ) { loadSingle(context) }
                }
            }
        }
    }
}