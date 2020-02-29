package it.speedcubing.flaubook.fragment

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import it.speedcubing.flaubook.Injector
import it.speedcubing.flaubook.R
import it.speedcubing.flaubook.connection.ConnectionAction
import it.speedcubing.flaubook.interfaces.FragmentClick
import it.speedcubing.flaubook.tools.timeToStringShort
import it.speedcubing.flaubook.viewmodel.BookVM
import kotlin.math.absoluteValue

class BookFragment : Fragment() {

    private lateinit var bookModel: BookVM
    private lateinit var picture: ImageView
    private lateinit var seekBar: SeekBar
    private lateinit var progressTime: TextView
    private lateinit var duration: TextView
    private lateinit var title: TextView
    private lateinit var previousChapter: Button
    private lateinit var nextChapter: Button
    private lateinit var moveForward: Button
    private lateinit var moveBack: Button
    private lateinit var playButton: MaterialButton
    private lateinit var mDetectorCompat: GestureDetectorCompat
    private lateinit var chaptersList: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        activity?.run {
            bookModel = ViewModelProvider(
                this,
                Injector.provideBookModel(this)
            ).get(BookVM::class.java)
        }
        val view = inflater.inflate(R.layout.book_layout, container, false)

        mDetectorCompat = GestureDetectorCompat(this.activity, SwipeDetector())
        picture = view.findViewById(R.id.book_cover)
        picture.setOnTouchListener { _, event ->
            mDetectorCompat.onTouchEvent(event)
            true
        }

        playButton = view.findViewById(R.id.book_play_pause)
        seekBar = view.findViewById(R.id.book_seek_bar)
        progressTime = view.findViewById(R.id.book_progress_time)
        progressTime.text = getString(R.string.zero)
        duration = view.findViewById(R.id.book_duration)
        duration.text = getString(R.string.zero)
        title = view.findViewById(R.id.book_current_chapter)
        nextChapter = view.findViewById(R.id.book_skip_next)
        previousChapter = view.findViewById(R.id.book_skip_prev)
        moveForward = view.findViewById(R.id.book_sf_30)
        moveBack = view.findViewById(R.id.book_sb_30)
        chaptersList = view.findViewById(R.id.book_show_chapters)
        chaptersList.isEnabled = false



        bookModel.position.observe(this, Observer {
            val position = it.toInt()
            seekBar.progress = position
            progressTime.text = timeToStringShort(position)
        })

        bookModel.playPauseRes.observe(this, Observer {
            playButton.setIconResource(it)
        })

        bookModel.meta.observe(this, Observer { updateUI(it) })

        nextChapter.setOnClickListener { bookModel.sendAction(ConnectionAction.SKIP_NEXT) }
        previousChapter.setOnClickListener { bookModel.sendAction(ConnectionAction.SKIP_PREV) }
        moveForward.setOnClickListener { bookModel.sendAction(ConnectionAction.MOVE_FW) }
        moveBack.setOnClickListener { bookModel.sendAction(ConnectionAction.MOVE_BW) }
        playButton.setOnClickListener { bookModel.sendAction(ConnectionAction.PLAY_PAUSE) }
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
                bookModel.seekTo(this.progress)
            }
        }

    }

    private fun updateUI(meta: BookVM.NowPlayingMetadata) {
        seekBar.max = meta.duration ?: 100
        duration.text = timeToStringShort(meta.duration?:100)
        if (meta.image != null) {
            picture.setImageBitmap(meta.image)
        } else {
            picture.setImageResource(R.mipmap.ic_launcher)
        }
        title.text = meta.title ?: ""
        previousChapter.isEnabled = !meta.isFirst
        nextChapter.isEnabled = !meta.isLast

        chaptersList.setOnClickListener {
            (activity as FragmentClick).showChapters(meta.id, ((meta.chapter) ?: 1) - 1)
        }
        chaptersList.isEnabled = true
    }


    private inner class SwipeDetector : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(
            event1: MotionEvent,
            event2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val dx = (event2.x - event1.x).absoluteValue
            val dy = event2.y - event1.y
            if (dx < TOLERATED_X && velocityY >= MINIMUM_SPEED && dy > MINIMUM_Y) {
                activity?.onBackPressed()
                return true
            }
            return false
        }
    }


}

private const val MINIMUM_SPEED = 100
private const val TOLERATED_X = 200
private const val MINIMUM_Y = 250