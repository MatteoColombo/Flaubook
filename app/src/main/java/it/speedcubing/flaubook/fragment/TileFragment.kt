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
import it.speedcubing.flaubook.tools.timeToString
import it.speedcubing.flaubook.viewmodel.BookVM

class TileFragment : Fragment() {


    private lateinit var bookVM: BookVM
    private lateinit var image: ImageView
    private lateinit var playPause: MaterialButton
    private lateinit var rollBack: Button
    private lateinit var title: TextView
    private lateinit var remaining: TextView
    private var duration = 0


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
        val view = inflater.inflate(R.layout.main_play_tile, container, false)

        image = view.findViewById(R.id.tile_picture)
        playPause = view.findViewById(R.id.tile_play)
        playPause.setOnClickListener { bookVM.sendAction(ConnectionAction.PLAY_PAUSE) }
        rollBack = view.findViewById(R.id.tile_roll_back)
        rollBack.setOnClickListener { bookVM.sendAction(ConnectionAction.MOVE_BW) }
        title = view.findViewById(R.id.tile_title)
        remaining = view.findViewById(R.id.tile_remaining)
        bookVM.meta.observe(this, Observer { updateStatus(it) })
        bookVM.playPauseResMini.observe(this, Observer { playPause.setIconResource(it) })
        bookVM.position.observe(this, Observer {
            remaining.text = getString(
                R.string.remaining_time,
                timeToString(duration - it.toInt())
            )
        })

        return view
    }

    fun updateStatus(meta: BookVM.NowPlayingMetadata) {
        duration = meta.duration ?: 0
        title.text = meta.title
        image.setImageBitmap(meta.image)
    }
}