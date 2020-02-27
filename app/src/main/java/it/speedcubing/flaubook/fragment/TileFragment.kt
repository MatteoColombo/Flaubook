package it.speedcubing.flaubook.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import it.speedcubing.flaubook.Injector
import it.speedcubing.flaubook.R
import it.speedcubing.flaubook.connection.ConnectionAction
import it.speedcubing.flaubook.interfaces.FragmentClick
import it.speedcubing.flaubook.tools.timeToString
import it.speedcubing.flaubook.viewmodel.BookVM
import java.util.*

class TileFragment : Fragment() {


    private lateinit var bookVM: BookVM
    private lateinit var image: ImageView
    private lateinit var playPause: MaterialButton
    private lateinit var rollBack: Button
    private lateinit var book: TextView
    private lateinit var title: TextView
    private lateinit var remaining: TextView
    private var duration = 0
    private lateinit var callback: FragmentClick

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.run {
            bookVM =
                ViewModelProvider(this, Injector.provideBookModel(this)).get(BookVM::class.java)
        }
        super.onCreateView(inflater, container, savedInstanceState)
        callback = activity as FragmentClick

        val view = inflater.inflate(R.layout.main_play_tile, container, false)

        image = view.findViewById(R.id.tile_picture)
        playPause = view.findViewById(R.id.tile_play)
        playPause.setOnClickListener { bookVM.sendAction(ConnectionAction.PLAY_PAUSE) }
        rollBack = view.findViewById(R.id.tile_roll_back)
        rollBack.setOnClickListener { bookVM.sendAction(ConnectionAction.MOVE_BW) }
        book = view.findViewById(R.id.tile_book)
        title = view.findViewById(R.id.tile_title)
        remaining = view.findViewById(R.id.tile_remaining)
        bookVM.meta.observe(this, Observer { updateStatus(it, view) })
        bookVM.playPauseResMini.observe(this, Observer { playPause.setIconResource(it) })
        bookVM.position.observe(this, Observer {
            remaining.text = getString(
                R.string.remaining_time,
                timeToString(duration - it.toInt())
            )
        })

        return view
    }

    fun updateStatus(meta: BookVM.NowPlayingMetadata, view: View) {
        view.setOnClickListener { callback.bookSelected(UUID.fromString(meta.id)) }
        duration = meta.duration ?: 0
        book.text = meta.book
        title.text = meta.title
        image.setImageBitmap(meta.image)
    }


}