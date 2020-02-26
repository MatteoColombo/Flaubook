package it.speedcubing.flaubook.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.speedcubing.flaubook.Injector
import it.speedcubing.flaubook.R
import it.speedcubing.flaubook.adapter.CLAdapter
import it.speedcubing.flaubook.viewmodel.ChapterLVM
import it.speedcubing.flaubook.viewmodel.PlayerVM
import java.util.*

class CLFragment : Fragment() {

    private lateinit var clVM: ChapterLVM
    private lateinit var playerVM: PlayerVM
    private lateinit var chapterList: RecyclerView
    private var currentChapter = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val bookId = arguments?.getSerializable("book_id") as UUID
        currentChapter = arguments?.getInt("chapter_num") as Int

        activity?.run {
            clVM = ViewModelProvider(this).get(ChapterLVM::class.java)
            clVM.bookID.postValue(bookId)
            playerVM =
                ViewModelProvider(this, Injector.providePlayerModel(this)).get(PlayerVM::class.java)
        }
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.cl_layout, container, false)

        chapterList = view.findViewById(R.id.cl_list)
        chapterList.layoutManager = LinearLayoutManager(context)
        chapterList.adapter = CLAdapter(emptyList(), currentChapter) {}

        clVM.clLiveData.observe(this, Observer {
            chapterList.adapter = CLAdapter(it, currentChapter) { pos -> chapterSelected(pos) }
        })

        return view
    }

    private fun chapterSelected(position: Int) {
        playerVM.playSomething(clVM.bookID.value.toString(), position)
        activity?.onBackPressed()
    }
}