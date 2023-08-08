package com.erendogan.konumgunluk.roomDB

import androidx.room.Database
import androidx.room.RoomDatabase
import com.erendogan.konumgunluk.model.Konum

@Database(entities = [Konum::class], version = 1)
abstract class DB : RoomDatabase() {
    abstract fun konumDAO():KonumDAO
}