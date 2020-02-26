package it.speedcubing.flaubook.database

import androidx.room.TypeConverter
import java.util.*

class Converters{

    @TypeConverter
    fun fromUUID(uuid:UUID?):String? = uuid?.toString()

    @TypeConverter
    fun toUUID(uuid:String?): UUID = UUID.fromString(uuid)
}