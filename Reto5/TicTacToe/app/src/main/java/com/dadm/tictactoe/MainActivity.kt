package com.dadm.tictactoe

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.dadm.tictactoe.ui.theme.TicTacToeTheme
import com.google.android.engage.common.datamodel.Image
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacToeTheme {
                TicTacToe(this@MainActivity, this)
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

@Composable
private fun MsgControl(
    selectedIds: MutableState<Set<Int>>, enemyIds: MutableState<Set<Int>>, enemyTurn: Boolean, modifier: Modifier
) {
    val courierFamily = FontFamily(
        Font(R.font.courier_regular, FontWeight.Normal),
        Font(R.font.courier_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.courier_bold, FontWeight.Bold)
    )
    Surface(
        tonalElevation = 3.dp,
        modifier = modifier
            .padding(vertical = 8.dp)
            .fillMaxHeight(0.7f),
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
        Text(msg, fontFamily = courierFamily, fontSize = 40.sp, modifier = modifier
            .fillMaxWidth()
            .padding(30.dp), textAlign = TextAlign.Center)

        if(resultVal !== 0)
            Text("\n\n\nToca cualquier celda para iniciar una nueva partida", fontFamily = courierFamily, fontSize = 40.sp, modifier = modifier
                .fillMaxWidth()
                .padding(24.dp), textAlign = TextAlign.Center)

        if(enemyTurn)
            Image(
                painter = rememberAsyncImagePainter(R.drawable.enemyspinner, imageLoader),
                contentDescription = "Hello!!",
                modifier = Modifier
                    .fillMaxSize()
                    .scale(0.6F),
            )

    }
}

@Composable
private fun StatControl(
    playerScore: Int,
    machineScore: Int,
    modifier: Modifier
) {
    val courierFamily = FontFamily(
        Font(R.font.courier_regular, FontWeight.Normal),
        Font(R.font.courier_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.courier_bold, FontWeight.Bold)
    )
    Surface(
        tonalElevation = 3.dp,
        modifier = modifier.padding(vertical = 8.dp),
        color = Color(50,50,50),
        contentColor = Color.White,
    ) {
        Row(modifier = modifier.fillMaxWidth()) {
            Text(
                "Jugador\n${playerScore}",
                fontFamily = courierFamily,
                fontSize = 24.sp,
                modifier = modifier.fillMaxWidth(0.5f),
                textAlign = TextAlign.Center
            )
            Text(
                "Máquina\n${machineScore}",
                fontFamily = courierFamily,
                fontSize = 24.sp,
                modifier = modifier.fillMaxWidth(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GameGrid(level: String, modifier: Modifier = Modifier, context: Context) {
    val itemsData by rememberSaveable { mutableStateOf(List(9) { it }) }
    val selectedIds = rememberSaveable { mutableStateOf(emptySet<Int>()) }
    val enemyIds = rememberSaveable { mutableStateOf(emptySet<Int>()) }
    var scorePlayer by remember { mutableStateOf(0) }
    var scoreMachine by remember { mutableStateOf(0) }

    val turn = rememberSaveable { mutableStateOf(true) }

    val effects = SoundManager(context = context)

    Column() {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(itemsData, key = { it }) { id ->
                val selected = selectedIds.value.contains(id) || enemyIds.value.contains(id)
                var type = if (selectedIds.value.contains(id)) 1 else if (enemyIds.value.contains(id)) 2 else 0
                val col = if (type === 1) Color(90, 173, 117) else Color(232, 81, 81)

                GameItem(type, selected, col,
                    Modifier.clickable {
                        if(turn.value) {
                            turn.value = false
                            val moves = Moves()
                            if (moves.checkVictory(selectedIds, enemyIds) !== 0) {
                                selectedIds.value = selectedIds.value.minus(selectedIds.value)
                                enemyIds.value = enemyIds.value.minus(enemyIds.value)

                                if (((scoreMachine + scorePlayer).mod(2)) !== 0) {
                                    val enemyMove = when (level) {
                                        "Easy" -> moves.easyMove(itemsData, selectedIds, enemyIds)
                                        "Medium" -> moves.mediumMove(
                                            itemsData,
                                            selectedIds,
                                            enemyIds
                                        )

                                        else -> {
                                            moves.hardMove(itemsData, selectedIds, enemyIds)
                                        }
                                    }
                                    enemyIds.value = enemyIds.value.plus(enemyMove)
                                    effects.play("enemy")
                                }
                                turn.value = true
                            } else if (type === 0) {
                                effects.play("touch")
                                selectedIds.value = if (selected)
                                    selectedIds.value.minus(id)
                                else
                                    selectedIds.value.plus(id)

                                val chkVictory = moves.checkVictory(selectedIds, enemyIds)
                                if (chkVictory === 0) {
                                    val enemyMove = when (level) {
                                        "Easy" -> moves.easyMove(itemsData, selectedIds, enemyIds)
                                        "Medium" -> moves.mediumMove(
                                            itemsData,
                                            selectedIds,
                                            enemyIds
                                        )

                                        else -> {
                                            moves.hardMove(itemsData, selectedIds, enemyIds)
                                        }
                                    }

                                    Timer().schedule(1300) {
                                        enemyIds.value = enemyIds.value.plus(enemyMove)
                                        effects.play("enemy")

                                        if (moves.checkVictory(selectedIds, enemyIds) === 2) {
                                            effects.play("lose")
                                            scoreMachine++
                                        }

                                        turn.value = true
                                    }
                                } else if (chkVictory === 1) {
                                    effects.play("win")
                                    scorePlayer++
                                    turn.value = true
                                } else {
                                    effects.play("tie")
                                    turn.value = true
                                }
                            }
                        }
                    }
                )
            }
        }
        MsgControl(selectedIds, enemyIds, !turn.value, modifier)
        StatControl(scorePlayer, scoreMachine, modifier)
    }
}

private val levels = listOf("Easy", "Medium", "Hard")

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToe(context: Activity, ctx:Context){

    val (difficultyLevel, selectDifficulty) = remember { mutableStateOf(levels.first()) }
    val scope = rememberCoroutineScope()

    Scaffold(
        bottomBar = { BottomApp(selectDifficulty, context) }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if(difficultyLevel == "Easy") Color(96, 235, 124) else if(difficultyLevel == "Medium") Color(235, 205, 96) else Color(235, 141, 96)
        ) {
            GameGrid(level = difficultyLevel, context = ctx)
        }
    }
}

@Composable
private fun BottomApp(selectDifficulty: (String) -> Unit, context: Activity) {
    BottomAppBar (
        Modifier.height(50.dp),
        containerColor = Color.DarkGray
    ) {
        val (confirmExit, setConfirmExit) = remember { mutableStateOf(false) }
        val (isDifficultyVisible, setIsDifficultyVisible) = remember { mutableStateOf(false) }
        IconButton(onClick = { setIsDifficultyVisible(true)}) {
            Icon(Icons.Filled.Build, contentDescription = "Dificultad")
        }
        DropdownMenu(expanded = isDifficultyVisible, onDismissRequest = { setIsDifficultyVisible(false) }) {
            Drawer(selectDifficulty, setIsDifficultyVisible);
        }

        IconButton(onClick = { triggerRestart(context = context) }) {
            Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Nuevo juego")
        }

        Spacer(Modifier.weight(1f, true))

        IconButton(onClick = {
            setConfirmExit(true)
        }) {
            Icon(imageVector = Icons.Filled.Clear, contentDescription = "Salir")
        }
        ConfirmAlert(confirmExit, setConfirmExit, "¿Seguro qué quieres salir?", "¿Estás realmente seguro?")
    }
}

fun triggerRestart(context: Activity) {
    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
    if (context is Activity) {
        (context as Activity).finish()
    }
    Runtime.getRuntime().exit(0)
}

@Composable
private fun Drawer(selectDifficulty: (String) -> Unit, setVisibility: (Boolean) -> Unit) {
    val items = listOf("Easy", "Medium", "Hard")
    Column {
        items.forEach {item ->
            TextButton(onClick = { selectDifficulty(item); setVisibility(false) }) {
                Text(text = if(item == "Easy") "Fácil" else if (item == "Medium") "Medio" else "Difícil", modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
fun ConfirmAlert(openDialog: Boolean, setConfirm: (Boolean) -> Unit, title: String, txt: String) {
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
                                exitProcess(0)
                            }) {
                            Text("Sacame de aquí!")
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


@Preview(showBackground = true)
@Composable
fun GameGridPreview() {
    TicTacToeTheme {
        //GameGrid("Easy")
    }
}