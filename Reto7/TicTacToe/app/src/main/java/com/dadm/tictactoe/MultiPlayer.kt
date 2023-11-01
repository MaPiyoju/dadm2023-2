package com.dadm.tictactoe

import SocketHandler
import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.lang.Integer.parseInt
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.system.exitProcess

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MultiPlayer : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mSocket = SocketHandler.getSocket()
        val intent = this.intent
        val roomId = intent.getStringExtra("ROOM")
        val sessionId = intent.getStringExtra("SESSION")

        setContent {
            TicTacToeTheme {
                TicTacToe(this@MultiPlayer, this, mSocket, roomId!!, sessionId!!)
            }
        }
    }
}

@Composable
private fun GameItem(
    type: Int, selected: Boolean, color: Color, modifier: Modifier
) {
    Surface(
        tonalElevation = 3.dp,
        contentColor = color,
        color = Color(70,70,70),
        modifier = modifier.aspectRatio(1f)
    ) {
        if (selected) {
            if(type == 1)
                Icon(Icons.Default.Close, null)
            else if(type == 2)
                Icon(Icons.Default.AddCircle, null)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToe(context: Activity, ctx:Context, mSocket: Socket, roomId: String, sessionId: String){

    val config = LocalConfiguration.current
    var orientation = remember { mutableStateOf(config.orientation) }

    Scaffold(
        bottomBar = { BottomApp(ctx, mSocket, roomId, sessionId) }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(100, 100, 100)
        ) {
            GameGrid(context = ctx, orientationMode = orientation, mSocket = mSocket, roomId = roomId, sessionId = sessionId)
        }
    }
}

@Composable
fun GameGrid(modifier: Modifier = Modifier, context: Context, orientationMode: MutableState<Int>, mSocket: Socket, roomId: String, sessionId: String) {
    val itemsData by rememberSaveable { mutableStateOf(List(9) { it }) }
    val selectedIds = rememberSaveable { mutableStateOf(emptySet<Int>()) }
    val enemyIds = rememberSaveable { mutableStateOf(emptySet<Int>()) }
    var scorePlayer by rememberSaveable { mutableStateOf(0) }
    var scoreMachine by rememberSaveable { mutableStateOf(0) }

    val turn = rememberSaveable { mutableStateOf(false) }

    mSocket.on("first_turn") {
        selectedIds.value = selectedIds.value.minus(selectedIds.value)
        enemyIds.value = enemyIds.value.minus(enemyIds.value)
        turn.value = true
    }

    mSocket.on("clear_turn") {
        selectedIds.value = selectedIds.value.minus(selectedIds.value)
        enemyIds.value = enemyIds.value.minus(enemyIds.value)
    }

    mSocket.on("start_turn") {args ->
        if (args[0] != null) {
            enemyIds.value = enemyIds.value.plus(parseInt(args[0].toString()))
            turn.value = true
        }
    }

    mSocket.on("empty_room") {
        mSocket.emit("clear_room", roomId)

        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("SESSION", sessionId)
        context.startActivity(intent)
    }

    val effects = SoundManager(context = context)

    if(orientationMode.value == Configuration.ORIENTATION_PORTRAIT) {
        Column() {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(itemsData, key = { it }) { id ->
                    val selected = selectedIds.value.contains(id) || enemyIds.value.contains(id)
                    var type =
                        if (selectedIds.value.contains(id)) 1 else if (enemyIds.value.contains(id)) 2 else 0
                    val col = if (type === 1) Color(90, 173, 117) else Color(232, 81, 81)

                    GameItem(type, selected, col,
                        Modifier.clickable {
                            if (turn.value) {
                                turn.value = false
                                val moves = Moves()
                                if (moves.checkVictory(selectedIds, enemyIds) !== 0) {
                                    mSocket.emit("wait_new", roomId)
                                }else {
                                    effects.play("touch")
                                    selectedIds.value = if (selected)
                                        selectedIds.value.minus(id)
                                    else
                                        selectedIds.value.plus(id)

                                    val chkVictory = moves.checkVictory(selectedIds, enemyIds)
                                    if (chkVictory === 1) {
                                        effects.play("win")
                                        scorePlayer++

                                        turn.value = true
                                    } else if (chkVictory === 3) {
                                        effects.play("tie")
                                        turn.value = true
                                    }
                                    mSocket.emit("play_turn", JSONObject("{ room: $roomId, data: $id}"))
                                }
                            }
                        }
                    )
                }
            }
            MsgControl(selectedIds, enemyIds, !turn.value, modifier, orientationMode)
            StatControl(scorePlayer, scoreMachine, modifier, orientationMode)
        }
    }else{
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = modifier
                    .fillMaxWidth(0.4f)
                    .padding(6.dp),
            ) {
                items(itemsData, key = { it }) { id ->
                    val selected = selectedIds.value.contains(id) || enemyIds.value.contains(id)
                    var type =
                        if (selectedIds.value.contains(id)) 1 else if (enemyIds.value.contains(id)) 2 else 0
                    val col = if (type === 1) Color(90, 173, 117) else Color(232, 81, 81)

                    GameItem(type, selected, col,
                        Modifier.clickable {
                            if (turn.value) {
                                turn.value = false
                                val moves = Moves()
                                if (moves.checkVictory(selectedIds, enemyIds) !== 0) {
                                    mSocket.emit("wait_new", roomId)
                                }else {
                                    effects.play("touch")
                                    selectedIds.value = if (selected)
                                        selectedIds.value.minus(id)
                                    else
                                        selectedIds.value.plus(id)

                                    val chkVictory = moves.checkVictory(selectedIds, enemyIds)
                                    if (chkVictory === 1) {
                                        effects.play("win")
                                        scorePlayer++

                                        turn.value = true
                                    } else if (chkVictory === 3) {
                                        effects.play("tie")
                                        turn.value = true
                                    }
                                    mSocket.emit("play_turn", JSONObject("{ room: $roomId, data: $id}"))
                                }
                            }
                        }
                    )
                }
            }
            Column() {
                MsgControl(selectedIds, enemyIds, !turn.value, modifier, orientationMode)
                StatControl(scorePlayer, scoreMachine, modifier, orientationMode)
            }
        }
    }
}



@Composable
private fun MsgControl(
    selectedIds: MutableState<Set<Int>>, enemyIds: MutableState<Set<Int>>, enemyTurn: Boolean, modifier: Modifier, orientationMode: MutableState<Int>
) {
    val courierFamily = FontFamily(
        Font(R.font.courier_regular, FontWeight.Normal),
        Font(R.font.courier_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.courier_bold, FontWeight.Bold)
    )
    Surface(
        tonalElevation = 3.dp,
        modifier = modifier
            .padding(
                horizontal = if (orientationMode.value == Configuration.ORIENTATION_PORTRAIT) 0.dp else 3.dp,
                vertical = if (orientationMode.value == Configuration.ORIENTATION_PORTRAIT) 8.dp else 4.dp
            )
            .fillMaxHeight(if (orientationMode.value == Configuration.ORIENTATION_PORTRAIT) 0.7f else 0.69f),
        color = Color(50,50,50),
        contentColor = Color.White,
    ) {
        val imageLoader = ImageLoader.Builder(LocalContext.current)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()

        val moves = Moves()
        val resultVal = moves.checkVictory(selectedIds, enemyIds)
        val msg = if(resultVal === 1) "Has ganado!" else if (resultVal === 2) "Has perdido! >.<" else if (resultVal === 3) "Empate" else ""
        val fSize = if(orientationMode.value == Configuration.ORIENTATION_PORTRAIT) 40.sp else 30.sp

        Text(msg, fontFamily = courierFamily, fontSize = fSize, modifier = modifier
            .fillMaxWidth(1f)
            .padding(30.dp), textAlign = TextAlign.Center)

        if(resultVal !== 0)
            Text("\n\n\nToca cualquier celda para iniciar una nueva partida", fontFamily = courierFamily, fontSize = fSize, modifier = modifier
                .fillMaxWidth(1f)
                .padding(24.dp), textAlign = TextAlign.Center)

        if(enemyTurn)
            Image(
                painter = rememberAsyncImagePainter(R.drawable.enemyspinner, imageLoader),
                contentDescription = "Hello!!",
                modifier = Modifier
                    .fillMaxSize(1f)
                    .scale(0.6F),
            )

    }
}

@Composable
private fun StatControl(
    playerScore: Int,
    machineScore: Int,
    modifier: Modifier,
    orientationMode: MutableState<Int>
) {
    val courierFamily = FontFamily(
        Font(R.font.courier_regular, FontWeight.Normal),
        Font(R.font.courier_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.courier_bold, FontWeight.Bold)
    )
    Surface(
        tonalElevation = 3.dp,
        modifier = modifier.padding(vertical=1.dp),
        color = Color(50,50,50),
        contentColor = Color.White,
    ) {
        Row() {
            Text(
                "Jugador\n${playerScore}",
                fontFamily = courierFamily,
                fontSize = if(orientationMode.value == Configuration.ORIENTATION_PORTRAIT) 24.sp else 20.sp,
                modifier = modifier.fillMaxWidth(0.5f),
                textAlign = TextAlign.Center
            )
            Text(
                "Contendor\n${machineScore}",
                fontFamily = courierFamily,
                fontSize = if(orientationMode.value == Configuration.ORIENTATION_PORTRAIT) 24.sp else 20.sp,
                modifier = modifier.fillMaxWidth(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BottomApp(ctx: Context, mSocket: Socket, roomId: String, sessionId: String) {
    BottomAppBar (
        Modifier.height(50.dp),
        containerColor = Color.DarkGray
    ) {
        val (confirmExit, setConfirmExit) = remember { mutableStateOf(false) }

        Spacer(Modifier.weight(1f, true))

        IconButton(onClick = {
            setConfirmExit(true)
        }) {
            Icon(imageVector = Icons.Filled.Clear, contentDescription = "Salir")
        }
        ConfirmAlert(ctx, confirmExit, setConfirmExit, "¿Seguro qué quieres salir?", "¿Estás realmente seguro?", mSocket, roomId, sessionId)
    }
}

@Composable
fun ConfirmAlert(ctx:Context, openDialog: Boolean, setConfirm: (Boolean) -> Unit, title: String, txt: String, mSocket: Socket, roomId: String, sessionId: String) {
    MaterialTheme {
        Column {
            if (openDialog) {
                AlertDialog(
                    onDismissRequest = {
                        setConfirm(false)
                    },
                    title = {
                        Text(text = title)
                    },
                    text = {
                        Text(txt)
                    },
                    confirmButton = {
                        Button(

                            onClick = {
                                mSocket.emit("leave_room", roomId)

                                val intent = Intent(ctx, MainActivity::class.java)
                                intent.putExtra("SESSION", sessionId)
                                ctx.startActivity(intent)
                            }) {
                            Text("Sácame de aquí!")
                        }
                    },
                    dismissButton = {
                        Button(

                            onClick = {
                                setConfirm(false)
                            }) {
                            Text("Me quedo!")
                        }
                    }
                )
            }
        }

    }
}

