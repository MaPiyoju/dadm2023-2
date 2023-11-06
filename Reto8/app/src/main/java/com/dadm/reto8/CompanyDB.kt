package com.dadm.reto8

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Company::class],
    version = 1
)
abstract class CompanyDB: RoomDatabase() {

    abstract val dao: CompanyDao

}