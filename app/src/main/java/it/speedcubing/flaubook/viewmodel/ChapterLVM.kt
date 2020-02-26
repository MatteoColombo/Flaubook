package it.speedcubing.flaubook.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import it.speedcubing.flaubook.database.BookRepository
import it.speedcubing.flaubook.database.Chapter
import java.util.*
import java.util.concurrent.Executors

class ChapterLVM : ViewModel() {


    private val bookRepository = BookRepository.get()
    private val executor = Executors.newSingleThreadExecutor()
    val bookID = MutableLiveData<UUID>()
    val clLiveData = MutableLiveData<List<Chapter>>().apply { postValue(emptyList()) }


    val observer = Observer<UUID> {
        it?.run {
            executor.execute {
                clLiveData.postValue(bookRepository.getBookChapters(it))
            }
        }
    }

    init {
        bookID.observeForever(observer)
    }

    override fun onCleared() {
        bookID.removeObserver(observer)
    }


}