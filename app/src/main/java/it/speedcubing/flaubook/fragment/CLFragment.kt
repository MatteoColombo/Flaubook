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
import it.speedcubing.flaubook.viewmodel.MainVM
import java.util.*

class CLFragment : Fragment() {

    private lateinit var mainVM: MainVM
    private lateinit var clVM: ChapterLVM
    private lateinit var chapterList: RecyclerView
    private var chapterNum = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.run {
            mainVM =
                ViewModelProvider(this, Injector.provideMainViewModel(this)).get(MainVM::class.java)
            clVM = ViewModelProvider(this).get(ChapterLVM::class.java)
        }
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.cl_layout, container, false)

        chapterList = view.findViewById(R.id.cl_list)
        chapterList.layoutManager = LinearLayoutManager(this.activity)
        chapterList.adapter = CLAdapter(emptyList(), -1) { position -> chapterSelected(position) }
        mainVM.meta.observe(this, Observer {
            chapterNum = it.chapter!!.toInt() - 1
            clVM.bookID.postValue(UUID.fromString(it.id))
        })

        clVM.clLiveData.observe(this, Observer {
            chapterList.adapter =
                CLAdapter(it, chapterNum) { position -> chapterSelected(position) }
        })


        return view
    }

    private fun chapterSelected(position: Int) {
        callback.chapterSelected()
        mainVM.playSomething(mainVM.meta.value!!.id, position)
    }

    interface ChapterSelected {
        fun chapterSelected()
    }

    companion object {
        private lateinit var callback: ChapterSelected
        fun initialize(parent: ChapterSelected) {
            callback = parent
        }

    }
}