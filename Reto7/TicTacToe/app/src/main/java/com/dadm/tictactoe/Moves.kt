package com.dadm.tictactoe

import android.util.Log
import androidx.compose.runtime.MutableState
import kotlin.random.Random

class Moves {

    fun easyMove(
        itemsData: List<Int>,
        selectedIds: MutableState<Set<Int>>,
        enemyIds: MutableState<Set<Int>>
    ): Int {
        val available = itemsData.minus(selectedIds.value).minus(enemyIds.value)
        val randomIndex = Random.nextInt(available.size);
        return available[randomIndex];
    }

    fun mediumMove(
        itemsData: List<Int>,
        selectedIds: MutableState<Set<Int>>,
        enemyIds: MutableState<Set<Int>>
    ): Int {
        var newMove = easyMove(itemsData, selectedIds, enemyIds)
        for (i in 0..7 step 3) {
            if(enemyIds.value.contains(i) && enemyIds.value.contains(i+1) && !selectedIds.value.contains(i+2)) {
                newMove = i + 2
                break
            }
            if(enemyIds.value.contains(i+1) && enemyIds.value.contains(i+2) && !selectedIds.value.contains(i)) {
                newMove = i
                break
            }
            if(enemyIds.value.contains(i) && enemyIds.value.contains(i+2) && !selectedIds.value.contains(i+1)) {
                newMove = i + 1
                break
            }
        }
        for (i in 0..3) {
            if(enemyIds.value.contains(i) && enemyIds.value.contains(i+3) && !selectedIds.value.contains(i+6)) {
                newMove = i + 6
                break
            }
            if(enemyIds.value.contains(i+3) && enemyIds.value.contains(i+6) && !selectedIds.value.contains(i)) {
                newMove = i
                break
            }
            if(enemyIds.value.contains(i) && enemyIds.value.contains(i+6) && !selectedIds.value.contains(i+3)) {
                newMove = i + 3
                break
            }
        }
        var diagA = intArrayOf(8,0,4)
        for (i in 0..8 step 4) {
            var secVal = if(i+4 > 8) 0 else i+4
            if(enemyIds.value.contains(i) && enemyIds.value.contains(secVal) && !selectedIds.value.contains(diagA[i/4])) {
                newMove = diagA[i/4]
                break
            }
        }
        var diagB = intArrayOf(6,2,4)
        for (i in 2..6 step 2) {
            var secVal = if(i+2 > 6) 2 else i+2
            if(enemyIds.value.contains(i) && enemyIds.value.contains(secVal) && !selectedIds.value.contains(diagB[(i/2)-1])) {
                newMove = diagB[(i/2)-1]
                break
            }
        }
        return newMove
    }

    fun hardMove(
        itemsData: List<Int>,
        selectedIds: MutableState<Set<Int>>,
        enemyIds: MutableState<Set<Int>>
    ): Int {
        var newMove = easyMove(itemsData, selectedIds, enemyIds)
        for (i in 0..7 step 3) {
            if(selectedIds.value.contains(i) && selectedIds.value.contains(i+1) && !enemyIds.value.contains(i+2)) {
                newMove = i + 2
                break
            }
            if(selectedIds.value.contains(i+1) && selectedIds.value.contains(i+2) && !enemyIds.value.contains(i)) {
                newMove = i
                break
            }
            if(selectedIds.value.contains(i) && selectedIds.value.contains(i+2) && !enemyIds.value.contains(i+1)) {
                newMove = i + 1
                break
            }
        }
        for (i in 0..3) {
            if(selectedIds.value.contains(i) && selectedIds.value.contains(i+3) && !enemyIds.value.contains(i+6)) {
                newMove = i + 6
                break
            }
            if(selectedIds.value.contains(i+3) && selectedIds.value.contains(i+6) && !enemyIds.value.contains(i)) {
                newMove = i
                break
            }
            if(selectedIds.value.contains(i) && selectedIds.value.contains(i+6) && !enemyIds.value.contains(i+3)) {
                newMove = i + 3
                break
            }
        }
        var diagA = intArrayOf(8,0,4)
        for (i in 0..8 step 4) {
            var secVal = if(i+4 > 8) 0 else i+4
            if(selectedIds.value.contains(i) && selectedIds.value.contains(secVal) && !enemyIds.value.contains(diagA[i/4])) {
                newMove = diagA[i/4]
                break
            }
        }
        var diagB = intArrayOf(6,2,4)
        for (i in 2..6 step 2) {
            var secVal = if(i+2 > 6) 2 else i+2
            if(selectedIds.value.contains(i) && selectedIds.value.contains(secVal) && !enemyIds.value.contains(diagB[(i/2)-1])) {
                newMove = diagB[(i/2)-1]
                break
            }
        }
        return newMove
    }

    fun checkVictory(
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

}