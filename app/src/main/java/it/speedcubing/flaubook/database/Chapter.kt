package it.speedcubing.flaubook.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Chapter(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val bookId: UUID,
    val chapId: Int,
    val title: String,
    val file: String,
    val len: Int = 0,
    val listened: Int = 0
)