package it.speedcubing.flaubook

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.speedcubing.flaubook.filetools.ImportManager
import it.speedcubing.flaubook.fragment.BookFragment
import it.speedcubing.flaubook.fragment.BookList
import it.speedcubing.flaubook.fragment.CLFragment
import it.speedcubing.flaubook.fragment.TileFragment
import it.speedcubing.flaubook.interfaces.FragmentClick
import it.speedcubing.flaubook.tools.ThemeManager
import it.speedcubing.flaubook.viewmodel.PlayerVM
import java.util.*

private const val TAG = "FLAUBOOK"
private const val PERMISSION_REQUEST_CODE: Int = 3828
private const val PICKER_REQUEST_CODE: Int = 3828

class MainActivity : AppCompatActivity(), FragmentClick {


    private lateinit var fab: FloatingActionButton
    private lateinit var zipImportManager: ImportManager
    private lateinit var playerModel: PlayerVM
    private var fabVisible = true
    private val playTile = TileFragment()
    private lateinit var tileFrame: FrameLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        playerModel = ViewModelProvider(
            this,
            Injector.providePlayerModel(this)
        ).get(PlayerVM::class.java)

        setContentView(R.layout.main_layout)

        tileFrame = findViewById(R.id.tile_frame)


        val currFrag = supportFragmentManager.findFragmentById(R.id.main_frame)
        if (currFrag == null) {
            val fragment = BookList()
            supportFragmentManager.beginTransaction()
                .add(R.id.main_frame, fragment)
                .commit()
        }

        fab = findViewById(R.id.main_fab)
        fab.setOnClickListener { zipImportManager.start() }


        playerModel.isPlayPause.observe(this, Observer {
            val currTileFrag = supportFragmentManager.findFragmentById(R.id.tile_frame)
            when {
                currTileFrag == null && it -> addTileFrag()
                currTileFrag != null && !it -> removeTileFrag()
            }
        })

        supportFragmentManager.addOnBackStackChangedListener {
            when (supportFragmentManager.findFragmentById(R.id.main_frame)) {
                is BookList -> {
                    fab.show()
                    tileFrame.visibility = View.VISIBLE

                }
                else -> {
                    fab.hide()
                    tileFrame.visibility = View.GONE
                }
            }
            fabVisible = !fabVisible
        }

        zipImportManager = ImportManager(this)
    }


    private fun addTileFrag() {
        supportFragmentManager.beginTransaction().add(R.id.tile_frame, playTile).commit()
    }

    private fun removeTileFrag() {
        supportFragmentManager.beginTransaction().remove(playTile).commit()
    }

    override fun onRestart() {
        super.onRestart()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStart() {
        super.onStart()
        playerModel.connect()
    }

    override fun onStop() {
        super.onStop()
        playerModel.disconnect()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            menuInflater.inflate(R.menu.main_menu, menu)
        }
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


    override fun bookSelected(id: UUID) {
        playerModel.playSomething(id.toString())
        val fragment = BookFragment()
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.main_frame, fragment)
            .addToBackStack(null).commit()


    }

    override fun showChapters(id: String, position: Int) {
        val bundle = Bundle()
        bundle.putSerializable("book_id", UUID.fromString(id))
        bundle.putInt("chapter_num", position)
        val fragment = CLFragment()
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.main_frame, fragment)
            .addToBackStack(null).commit()

    }


}
