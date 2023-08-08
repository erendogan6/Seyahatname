package com.erendogan.konumgunluk.roomDB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.erendogan.konumgunluk.model.Konum
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

@Dao
interface KonumDAO {

    @Query(value = "SELECT * FROM Konum")
    fun veriAl() : Flowable<List<Konum>>

    @Insert
    fun ekle(konum: Konum) : Completable

    @Delete
    fun sil(konum: Konum) : Completable
}