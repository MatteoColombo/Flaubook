package it.speedcubing.flaubook.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.speedcubing.flaubook.R
import it.speedcubing.flaubook.adapter.BLAdapter
import it.speedcubing.flaubook.database.Book
import it.speedcubing.flaubook.viewmodel.BookLVM
import java.util.*

class BookList : Fragment() {

    private lateinit var list: RecyclerView
    private lateinit var adapter: BLAdapter
    private var callbacks: BLCallbacks? = null

    private val blViewModel: BookLVM by lazy {
        ViewModelProvider(this).get(BookLVM::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.bl_layout, container, false)
        val main_toolbar: Toolbar = view.findViewById(R.id.main_toolbar)
        (activity as AppCompatActivity).setSupportActionBar(main_toolbar)

        list = view.findViewById(R.id.bl_recycler)
        list.layoutManager = LinearLayoutManager(context)
        adapter = BLAdapter(
            emptyList()
        ) { book: Book, long: Boolean -> handleListItemClick(book, long) }
        list.adapter = adapter

        blViewModel.blLiveData.observe(this, Observer { updateUI(it) })

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as BLCallbacks
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun updateUI(books: List<Book>) {
        adapter = BLAdapter(books) { book: Book, long: Boolean ->
            handleListItemClick(
                book,
                long
            )
        }
        list.adapter = adapter
    }

    private fun handleListItemClick(book: Book, long: Boolean = false) {
        when (long) {
            false -> callbacks?.bookSelected(book.id)
            true -> createOptionDialog(book)
        }
    }

    interface BLCallbacks {
        fun bookSelected(id: UUID)
    }

    private fun createOptionDialog(book: Book) {
        MaterialAlertDialogBuilder(this.activity).apply {
            setTitle(getString(R.string.options))
            setItems(
                arrayOf(
                    getString(R.string.delete_book_option),
                    getString(R.string.mark_finished_option),
                    getString(
                        R.string.reset_book_option
                    )
                )
            ) { _, which ->
                when (which) {
                    0 -> showDeleteConfirmDialog(book)
                    1 -> blViewModel.finishBook(book)
                    2 -> blViewModel.resetBook(book)
                }
            }
        }.create().show()
    }

    private fun showDeleteConfirmDialog(book: Book) {
        MaterialAlertDialogBuilder(this.activity).apply {
            setTitle("Elimina")
            setMessage(getString(R.string.confirm_delete, book.title))
            setPositiveButton(getString(R.string.yes)) { _, _ -> blViewModel.deleteBook(book) }
            setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
        }.create().show()

    }


}