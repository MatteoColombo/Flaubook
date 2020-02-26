package it.speedcubing.flaubook.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.media.MediaMetadataCompat
import it.speedcubing.flaubook.database.Book
import it.speedcubing.flaubook.database.Chapter

data class ServiceBook(val bookId: String, val book: Book, val chapters: List<Chapter>) {


    fun getMetadata(index: Int): MediaMetadataCompat {
        val builder = MediaMetadataCompat.Builder().apply {
            id = bookId
            album = book.title
            title = chapters[index].title
            author = book.author
            artist = book.readBy
            duration = chapters[index].len.toLong()
            trackCount = book.chapNum.toLong()
            trackNumber = (index + 1).toLong()
            albumArt = Bitmap.createBitmap(BitmapFactory.decodeFile(book.picture))
        }

        return builder.build()
    }
}