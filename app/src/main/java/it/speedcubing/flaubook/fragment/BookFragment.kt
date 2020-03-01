package it.speedcubing.flaubook.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import it.speedcubing.flaubook.Injector
import it.speedcubing.flaubook.R
import it.speedcubing.flaubook.connection.ConnectionAction
import it.speedcubing.flaubook.tools.timeToStringShort
import it.speedcubing.flaubook.viewmodel.MainVM


class BookFragment : Fragment() {

    private lateinit var mainVM: MainVM
    private lateinit var image: ImageView
    private lateinit var chapter: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var progressTime: TextView
    private lateinit var remainingTime: TextView
    private lateinit var playPause: MaterialButton
    private lateinit var nextChapter: Button
    private lateinit var prevChapter: Button
    private lateinit var moveFw: Button
    private lateinit var moveBw: Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        activity?.run {
            mainVM =
                ViewModelProvider(this, Injector.provideMainViewModel(this)).get(MainVM::class.java)
        }
        val view = inflater.inflate(R.layout.book_layout, container, false)
        image = view.findViewById(R.id.book_cover)
        chapter = view.findViewById(R.id.book_current_chapter)
        seekBar = view.findViewById(R.id.book_seek_bar)
        progressTime = view.findViewById(R.id.book_progress_time)
        remainingTime = view.findViewById(R.id.book_remaining_time)
        playPause = view.findViewById(R.id.book_play_pause)
        nextChapter = view.findViewById(R.id.book_skip_next)
        prevChapter = view.findViewById(R.id.book_skip_prev)
        moveFw = view.findViewById(R.id.book_sf_30)
        moveBw = view.findViewById(R.id.book_sb_30)

        mainVM.meta.observe(this, Observer { updateUI(it) })
        mainVM.position.observe(this, Observer {
            val intTime = it.toInt()
            seekBar.progress = intTime
            progressTime.text = timeToStringShort(intTime)
            remainingTime.text = "-${timeToStringShort(seekBar.max - intTime)}"
        })

        mainVM.playPauseRes.observe(this, Observer {
            playPause.setIconResource(it)
        })

        playPause.setOnClickListener { mainVM.sendAction(ConnectionAction.PLAY_PAUSE) }
        nextChapter.setOnClickListener { mainVM.sendAction(ConnectionAction.SKIP_NEXT) }
        prevChapter.setOnClickListener { mainVM.sendAction(ConnectionAction.SKIP_PREV) }
        moveFw.setOnClickListener { mainVM.sendAction(ConnectionAction.MOVE_FW) }
        moveBw.setOnClickListener { mainVM.sendAction(ConnectionAction.MOVE_BW) }
        seekBar.setOnSeekBarChangeListener(Seek())
        return view
    }

    private inner class Seek : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            seekBar?.run {
                mainVM.seekTo(this.progress)
            }
        }

    }

    private fun updateUI(meta: MainVM.NowPlayingMetadata) {
        chapter.text = meta.title
        seekBar.max = meta.duration!!.toInt()
        image.setImageBitmap(meta.image)
        nextChapter.isEnabled = !meta.isLast
        prevChapter.isEnabled = !meta.isFirst
    }
}