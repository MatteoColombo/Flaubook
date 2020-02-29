package it.speedcubing.flaubook.filetools

import android.content.Context
import android.media.MediaMetadataRetriever
import it.speedcubing.flaubook.database.Book
import it.speedcubing.flaubook.database.BookRepository
import it.speedcubing.flaubook.database.Chapter
import it.speedcubing.flaubook.tools.timeToString
import org.json.JSONObject
import java.io.File
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

private val regex = Regex("^[0-9]*\\.mp3$")

fun importZip(path: String, context: Context): Boolean {
    val folderName = UUID.randomUUID().toString()
    val folderPath = "${context.getExternalFilesDir(null)}/$folderName"
    val folder = File(folderPath)

    folder.mkdir()

    val archive = File(path)
    val chapterFiles = mutableListOf<Pair<String, Int>>()

    var wellFormed = true
    ZipFile(archive).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            wellFormed = checkZipEntry(entry)
            if (wellFormed) {
                val toSave = File("$folderPath/${entry.name}")
                zip.getInputStream(entry).use { input ->
                    toSave.outputStream().use { output ->
                        input.copyTo(output)
                        output.close()
                    }
                    input.close()
                }
                if (entry.name !in setOf("cover.jpg", "book.json")) {
                    val mdr = MediaMetadataRetriever()
                    mdr.setDataSource(toSave.path)
                    chapterFiles.add(
                        Pair(
                            entry.name,
                            mdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toInt()
                        )
                    )

                }
            } else {
                return@use
            }
        }
        zip.close()
    }

    deleteFile(file = archive)

    var bookJSON: JSONObject? = null

    if (wellFormed) {
        var json: String = ""
        File("$folderPath/book.json").forEachLine { json += it }
        bookJSON = JSONObject(json)
        wellFormed = checkBookJSON(bookJSON)
    }

    if (wellFormed) {
        chapterFiles.sortBy { it.first }
        val bookLen = chapterFiles.fold(0) { sum, it -> it.second + sum }
        val book = Book(
            title = bookJSON!!.getString("title"),
            collection = if (bookJSON.has("collection")) bookJSON.getString("collection") else bookJSON.getString(
                "title"
            ),
            collectionNum = if (bookJSON.has("collectionNum")) bookJSON.getInt("collectionNum") else 0,
            readBy = bookJSON.getString("readBy"),
            author = bookJSON.getString("author"),
            folder = folderPath,
            chapNum = bookJSON.getInt("chaptersNum"),
            picture = "$folderPath/cover.jpg",
            len = bookLen,
            strLen = timeToString(bookLen)
        )
        BookRepository.get().addBook(book)
        val chapters: List<Chapter> = chapterFiles.mapIndexed { index, it ->
            Chapter(
                chapId = index,
                title = bookJSON.getJSONArray("chapters").getString(index),
                bookId = book.id,
                file = "${folderPath}/${it.first}",
                len = it.second
            )
        }
        BookRepository.get().addChapters(chapters)
        return true
    } else {
        deleteFile(folder)
        return false
    }
}

private fun checkZipEntry(entry: ZipEntry): Boolean {
    return when {
        entry.name in setOf("cover.jpg", "book.json") -> true
        regex.matches(entry.name) -> true
        else -> false
    }
}


private fun checkBookJSON(json: JSONObject): Boolean =
    (json.has("title") && json.has("author") && json.has("readBy")
            && json.has("chapters") && json.has("chaptersNum")
            && (json.getJSONArray("chapters").length() == json.getInt("chaptersNum")))

