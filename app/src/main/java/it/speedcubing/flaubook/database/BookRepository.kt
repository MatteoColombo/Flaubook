package it.speedcubing.flaubook.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import it.speedcubing.flaubook.filetools.deleteFile
import java.util.*
import java.util.concurrent.Executors

private const val DB_NAME = "flaubook-database"

class BookRepository private constructor(context: Context) {

    private val database: Database = Room.databaseBuilder(
        context.applicationContext, Database::class.java,
        DB_NAME
    ).build()

    private val bookDao = database.bookDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getBooks(): LiveData<List<Book>> = bookDao.getBooks()

    fun getBookChapters(id: UUID): List<Chapter> = bookDao.getBookChapters(id)

    fun addBook(book: Book) = executor.execute {
        bookDao.addBook(book)
    }

    fun addChapters(chapters: List<Chapter>) = executor.execute {
        bookDao.addChapters(chapters)
    }

    fun deleteBook(book: Book) = executor.execute {
        bookDao.deleteBook(book.id)
        bookDao.deleteBookChapters(book.id)
        deleteFile(path = book.folder)
    }

    fun markBookAsFinished(book: Book) = executor.execute {
        bookDao.markBookFinished(book.id)
        bookDao.markChaptersFinished(book.id)
    }

    fun resetBook(book: Book) = executor.execute {
        bookDao.resetBook(book.id)
        bookDao.resetChapters(book.id)
    }

    fun moveToNextChapter(chapter: Chapter) = executor.execute {
        bookDao.markChaptersFinished(chapter.id)
        bookDao.selectChapter(chapter.bookId, chapter.chapId + 1)
        bookDao.setChapterProgress(chapter.id, 0)
        updateBookTime(chapter.bookId, chapter.chapId + 1)
    }

    fun selectChapter(chapter: Chapter, startTime: Int = 0) = executor.execute {
        bookDao.selectChapter(chapter.bookId, chapter.chapId )
        bookDao.setChapterProgress(chapter.id, startTime)
        updateBookTime(chapter.bookId, chapter.chapId , startTime)
    }

    fun saveChapterProgress(chapter: Chapter, listened: Int) =
        executor.execute {
            bookDao.setChapterProgress(chapter.id, listened)
            updateBookTime(chapter.bookId, chapter.chapId, listened)
        }

    private fun updateBookTime(id: UUID, chapter: Int, chapterTime: Int = 0) {
        val chapters = bookDao.getBookChapters(id)
        val bookTime =
            chapters.fold(0) { sum, it -> if (it.chapId < chapter) sum + it.len else sum }
        bookDao.setBookProgress(id, bookTime + chapterTime)
    }


    companion object {
        private var INSTANCE: BookRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = BookRepository(context)
            }
        }

        fun get(): BookRepository =
            INSTANCE ?: throw IllegalStateException("Repository must be initialized")
    }
}