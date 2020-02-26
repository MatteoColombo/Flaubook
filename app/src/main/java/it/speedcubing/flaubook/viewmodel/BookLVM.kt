package it.speedcubing.flaubook.viewmodel

import androidx.lifecycle.ViewModel
import it.speedcubing.flaubook.database.Book
import it.speedcubing.flaubook.database.BookRepository

class BookLVM : ViewModel() {

    private val bookRepository = BookRepository.get()
    val blLiveData = bookRepository.getBooks()

    fun deleteBook(book: Book) = bookRepository.deleteBook(book)


    fun resetBook(book: Book) = bookRepository.resetBook(book)
    fun finishBook(book: Book) = bookRepository.markBookAsFinished(book)

}