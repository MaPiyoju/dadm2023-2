package com.dadm.tictactoe

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.MutableState
import kotlin.random.Random

class SoundManager(context: Context) {

    private val effectsMap = mutableMapOf(
        "touch" to MediaPlayer.create(context, R.raw.touch),
        "win" to MediaPlayer.create(context, R.raw.win),
        "lose" to MediaPlayer.create(context, R.raw.lose),
        "tie" to MediaPlayer.create(context, R.raw.tie),
        "enemy" to MediaPlayer.create(context, R.raw.enemy)
    )

    fun play(key: String) {
        effectsMap[key]?.isLooping = false
        effectsMap[key]?.start()
    }

}