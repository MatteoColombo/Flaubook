package it.speedcubing.flaubook.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.*

@Dao
interface BookDao {

    @Insert
    fun addBook(book: Book)

    @Query("DELETE FROM book WHERE id=(:id)")
    fun deleteBook(id: UUID)

    @Query("SELECT * FROM book WHERE id=(:id)")
    fun getBook(id: UUID): LiveData<Book?>

    @Query("SELECT folder FROM BOOK WHERE id=(:id)")
    fun getBookPath(id: UUID): String

    @Query("SELECT * FROM book ORDER BY collection asc, collectionNum asc")
    fun getBooks(): LiveData<List<Book>>

    @Query("UPDATE book SET currChap=(chapNum-1), listened=len WHERE id=(:id)")
    fun markBookFinished(id: UUID)

    @Query("UPDATE book SET currChap=0, listened=0 WHERE id=(:id)")
    fun resetBook(id: UUID)

    @Query("UPDATE book SET currChap = (:chapter) WHERE id =(:id)")
    fun selectChapter(id: UUID, chapter: Int)

    @Query("UPDATE book SET listened = (:listened) WHERE id =(:id)")
    fun setBookProgress(id: UUID, listened: Int)

    /*
    -------------------------------------------
    CHAPTERS
     -------------------------------------------
     */

    @Insert
    fun addChapters(chapters: List<Chapter>)

    @Query("DELETE FROM chapter WHERE bookId=(:id)")
    fun deleteBookChapters(id: UUID)

    @Query("UPDATE chapter SET listened=len WHERE bookId=(:id)")
    fun markChaptersFinished(id: UUID)

    @Query("UPDATE chapter SET listened=0 WHERE bookId=(:id)")
    fun resetChapters(id: UUID)

    @Query("SELECT * FROM chapter WHERE bookId=(:id)")
    fun getBookChapters(id: UUID): List<Chapter>

    @Query("UPDATE chapter SET listened = (:listened) WHERE id =(:id)")
    fun setChapterProgress(id: UUID, listened: Int)


}
