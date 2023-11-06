package com.dadm.reto8

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Company(
    val name: String,
    val url: String,
    val phone: String,
    val email: String,
    val products: String,
    val classification: String,

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)
