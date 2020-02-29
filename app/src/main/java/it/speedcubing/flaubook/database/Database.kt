package it.speedcubing.flaubook.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Book::class, Chapter::class], version =2,exportSchema = false)
@TypeConverters(Converters::class)
abstract class Database : RoomDatabase() {

    abstract fun bookDao(): BookDao

}
