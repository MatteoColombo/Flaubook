package it.speedcubing.flaubook.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Book(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val title: String,
    val collection: String,
    val collectionNum: Int,
    val author: String,
    val readBy: String,
    val chapNum: Int,
    val folder: String,
    val picture: String,
    val currChap: Int = 0,
    val len: Int = 0,
    val listened: Int = 0,
    val strLen:String
)