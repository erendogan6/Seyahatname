package com.erendogan.konumgunluk.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class Konum(
    @ColumnInfo(name="isim")
    var isim : String,

    @ColumnInfo(name="enlem")
    var enlem: Double,

    @ColumnInfo(name="boylam")
    var boylam: Double) : Serializable

{
    @PrimaryKey(autoGenerate = true)
    var id = 0
}