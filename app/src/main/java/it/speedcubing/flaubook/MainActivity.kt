package it.speedcubing.flaubook

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.updateMargins
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.speedcubing.flaubook.adapter.BLAdapter
import it.speedcubing.flaubook.connection.ConnectionAction
import it.speedcubing.flaubook.database.Book
import it.speedcubing.flaubook.filetools.ImportManager
import it.speedcubing.flaubook.fragment.BookFragment
import it.speedcubing.flaubook.tools.ThemeManager
import it.speedcubing.flaubook.viewmodel.MainVM
import kotlin.math.absoluteValue

private const val PERMISSION_REQUEST_CODE: Int = 3828
private const val PICKER_REQUEST_CODE: Int = 3828

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var fab: FloatingActionButton
    private lateinit var zipImportManager: ImportManager
    private lateinit var mainVM: MainVM
    private lateinit var bookList: RecyclerView
    private lateinit var bottomBar: BottomAppBar
    private lateinit var bottomPlayPause: MaterialButton
    private lateinit var bottomProgress: ProgressBar
    private lateinit var mDetectorCompat: GestureDetectorCompat


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Get view model */
        mainVM =
            ViewModelProvider(this, Injector.provideMainViewModel(this)).get(MainVM::class.java)

        /* Set content and appbar */
        setContentView(R.layout.main_layout)
        toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)


        /* Init FAB and ZIP importer */
        fab = findViewById(R.id.main_fab)
        fab.setOnClickListener { zipImportManager.start() }
        zipImportManager = ImportManager(this)

        /* Init booklist */
        bookList = findViewById(R.id.main_book_list)
        bookList.layoutManager = LinearLayoutManager(this)
        bookList.adapter = BLAdapter(emptyList())
        mainVM.bookListLD.observe(this, Observer {
            bookList.adapter =
                BLAdapter(it) { book: Book, b: Boolean -> bookListItemClick(book, b) }
        })

        /* Init bottom bar */
        bottomBar = findViewById(R.id.bottom_appbar)
        mainVM.isPlayPause.observe(this, Observer {
            when (it) {
                true -> {
                    val params = fab.layoutParams as CoordinatorLayout.LayoutParams
                    params.updateMargins(bottom = (75 * getResources().getDisplayMetrics().density).toInt())
                    fab.layoutParams = params
                    bottomBar.visibility = View.VISIBLE
                }
                else ->{
                    val params = fab.layoutParams as CoordinatorLayout.LayoutParams
                    params.updateMargins(bottom = (16 * getResources().getDisplayMetrics().density).toInt())
                    fab.layoutParams = params
                    bottomBar.visibility = View.GONE
                }
            }
        })
        bottomBar.setOnClickListener {
            val fragment: BottomSheetDialogFragment = BookFragment()
            fragment.show(supportFragmentManager, fragment.tag)
        }
        mDetectorCompat = GestureDetectorCompat(this, SwipeDetector())
        bottomBar.setOnTouchListener { _, event ->
            mDetectorCompat.onTouchEvent(event)
        }

        /* Register to meta and state */
        bottomPlayPause = findViewById(R.id.bottom_play_pause)
        bottomPlayPause.setOnClickListener { mainVM.sendAction(ConnectionAction.PLAY_PAUSE) }
        bottomProgress = findViewById(R.id.bottom_progress)
        mainVM.meta.observe(this, Observer { updateUI(it) })
        mainVM.playPauseResMini.observe(this, Observer { bottomPlayPause.setIconResource(it) })
        mainVM.position.observe(this, Observer {
            val intPos = it.toInt()
            bottomProgress.progress = intPos
        })
    }

    override fun onRestart() {
        super.onRestart()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStart() {
        super.onStart()
        mainVM.connect()
    }

    override fun onStop() {
        super.onStop()
        mainVM.disconnect()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.main_app_bar_theme -> ThemeManager.setTheme(
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_YES
                    Configuration.UI_MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_NO
                    else -> AppCompatDelegate.MODE_NIGHT_NO
                }
            )
        }
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (PICKER_REQUEST_CODE == requestCode && Activity.RESULT_OK == resultCode && data != null) {
            zipImportManager.handleFile(data)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == PERMISSION_REQUEST_CODE) {
            zipImportManager.gotPermission()
        } else {
            finish()
        }
    }


    private fun bookListItemClick(book: Book, isLong: Boolean) {
        when (isLong) {
            false -> {
                mainVM.playSomething(book.id.toString())
                val fragment: BottomSheetDialogFragment = BookFragment()
                fragment.show(supportFragmentManager, fragment.tag)
            }
            true -> createOptionDialog(book)
        }

    }

    private fun updateUI(meta: MainVM.NowPlayingMetadata) {
        updateBottomBar(meta)
    }

    private fun updateBottomBar(meta: MainVM.NowPlayingMetadata) {
        findViewById<ImageView>(R.id.bottom_picture).setImageBitmap(meta.image)
        findViewById<TextView>(R.id.bottom_title).text = "${meta.title} - ${meta.book}"
        bottomProgress.max = meta.duration ?: 100
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
            Log.i("SWIPER", "$dx $dy $velocityY")
            if (dx < TOLERATED_X && velocityY <= MINIMUM_SPEED && dy <= MINIMUM_Y) {
                val fragment: BottomSheetDialogFragment = BookFragment()
                fragment.show(supportFragmentManager, fragment.tag)
                return true
            }
            return false
        }

    }

    private fun createOptionDialog(book: Book) {
        MaterialAlertDialogBuilder(this).apply {
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
                    1 -> mainVM.finishBook(book)
                    2 -> mainVM.resetBook(book)
                }
            }
        }.create().show()
    }

    private fun showDeleteConfirmDialog(book: Book) {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("Elimina")
            setMessage(getString(R.string.confirm_delete, book.title))
            setPositiveButton(getString(R.string.yes)) { _, _ -> mainVM.deleteBook(book) }
            setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
        }.create().show()

    }

}

private const val MINIMUM_SPEED = -100
private const val TOLERATED_X = 200
private const val MINIMUM_Y = -100