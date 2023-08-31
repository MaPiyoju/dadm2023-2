package com.dadm.tictactoe

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dadm.tictactoe.ui.theme.TicTacToeTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacToeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(40,40,40)
                ) {
                    GameGrid()
                }
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

private fun checkVictory(
    selectedIds: MutableState<Set<Int>>, enemyIds: MutableState<Set<Int>>
): Int {
    var result = 0;
    val winOpts: Array<Collection<Int>> = arrayOf(
        listOf(0, 1, 2),
        listOf(3, 4, 5),
        listOf(6, 7, 8),
        listOf(0, 3, 6),
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        listOf(0, 4, 8),
        listOf(2, 4, 6)
    )

    winOpts.forEach { item ->
        if (selectedIds.value.containsAll(item))
            result = 1

        if (enemyIds.value.containsAll(item))
            result = 2
    }

    if(selectedIds.value.size + enemyIds.value.size === 9 && result === 0)
        result = 3

    return result;
}

@Composable
private fun MsgControl(
    selectedIds: MutableState<Set<Int>>, enemyIds: MutableState<Set<Int>>, modifier: Modifier
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
        val resultVal = checkVictory(selectedIds, enemyIds)
        val msg = if(resultVal === 1) "Has ganado!" else if (resultVal === 2) "Has perdido! >.<" else if (resultVal === 3) "Empate" else ""
        Text(msg, fontFamily = courierFamily, fontSize = 40.sp, modifier = modifier
            .fillMaxWidth()
            .padding(30.dp), textAlign = TextAlign.Center)

        if(resultVal !== 0)
            Text("\n\n\nToca cualquier celda para iniciar una nueva partida", fontFamily = courierFamily, fontSize = 40.sp, modifier = modifier
                .fillMaxWidth()
                .padding(24.dp), textAlign = TextAlign.Center)

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
fun GameGrid(modifier: Modifier = Modifier) {
    val itemsData by rememberSaveable { mutableStateOf(List(9) { it }) }
    val selectedIds = rememberSaveable { mutableStateOf(emptySet<Int>()) }
    val enemyIds = rememberSaveable { mutableStateOf(emptySet<Int>()) }
    var scorePlayer by remember { mutableStateOf(0) }
    var scoreMachine by remember { mutableStateOf(0) }

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
                        if(checkVictory(selectedIds, enemyIds) !== 0){
                            selectedIds.value = selectedIds.value.minus(selectedIds.value)
                            enemyIds.value = enemyIds.value.minus(enemyIds.value)

                            if(((scoreMachine+scorePlayer).mod(2)) !== 0){
                                val available = itemsData.minus(selectedIds.value).minus(enemyIds.value)
                                val randomIndex = Random.nextInt(available.size);
                                val enemyPlay = available[randomIndex]
                                enemyIds.value = enemyIds.value.plus(enemyPlay)
                            }
                        }else if (type === 0) {
                            selectedIds.value = if (selected)
                                selectedIds.value.minus(id)
                            else
                                selectedIds.value.plus(id)

                            val chkVictory = checkVictory(selectedIds, enemyIds)
                            if(chkVictory === 0) {
                                val available = itemsData.minus(selectedIds.value).minus(enemyIds.value)
                                val randomIndex = Random.nextInt(available.size);
                                val enemyPlay = available[randomIndex]
                                enemyIds.value = enemyIds.value.plus(enemyPlay)

                                if(checkVictory(selectedIds, enemyIds) === 2)
                                    scoreMachine++
                            }else if(chkVictory === 1){
                                scorePlayer++
                            }
                        }
                    }
                )
            }
        }
        MsgControl(selectedIds, enemyIds, modifier)
        StatControl(scorePlayer, scoreMachine, modifier)
    }
}

@Preview(showBackground = true)
@Composable
fun GameGridPreview() {
    TicTacToeTheme {
        GameGrid()
    }
}