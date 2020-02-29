package it.speedcubing.flaubook.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.media.MediaBrowserServiceCompat
import it.speedcubing.flaubook.database.Book
import it.speedcubing.flaubook.database.BookRepository
import it.speedcubing.flaubook.database.Chapter
import java.util.concurrent.Executors

class PlayerService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManagerCompat
    private val bookRepository = BookRepository.get()
    private lateinit var booksLiveData: LiveData<List<Book>>
    private lateinit var bookObserver: Observer<List<Book>>
    private lateinit var books: List<ServiceBook>
    private lateinit var noisyObserver: NoisyObserver
    private var currentBook: ServiceBook? = null
    private lateinit var player: FlaubookPlayer

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.detach()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? = BrowserRoot("@empty", null)


    override fun onCreate() {
        super.onCreate()
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        mediaSession = MediaSessionCompat(
            this,
            TAG
        )
        player = FlaubookPlayer(this, mediaSession)

        mediaSession.setSessionActivity(sessionActivityPendingIntent)
        mediaSession.setCallback(MSCallbacks(this))
        sessionToken = mediaSession.sessionToken
        notificationManager = NotificationManagerCompat.from(this)

        booksLiveData = bookRepository.getBooks()
        bookObserver = getBookObserver()
        booksLiveData.observeForever(bookObserver)
        noisyObserver = NoisyObserver(this, mediaSession.sessionToken)

    }


    private fun getBookObserver(): Observer<List<Book>> = Observer { books ->
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            this.books = books.map { book ->
                val chapters: List<Chapter> = bookRepository.getBookChapters(book.id)
                ServiceBook(book.id.toString(), book, chapters)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaSession.controller.playbackState.state != PlaybackStateCompat.STATE_NONE) {
            player.stop()
        }
        mediaSession.release()
        booksLiveData.removeObserver(bookObserver)
    }

    private inner class MSCallbacks(context: Context) : MediaSessionCompat.Callback(),
        FlaubookPlayer.PlayerCallback {

        private var isForeground = false
        private val notifBuilder = NotificationBuilder(context)

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            val chapter: Int? = extras?.getInt("chapter_id")
            val book = books.find { it.bookId == mediaId }
            if (book != null) {
                with(currentBook) {
                    val bookFinished = book.book.len == book.book.listened
                    val cc =
                        if (chapter != null && chapter >= 0) chapter else if (bookFinished) 0 else book.book.currChap
                    val ch = book.chapters[cc]
                    val chapterFinished = ch.len == ch.listened
                    val startAt = if (chapterFinished) 0 else ch.listened
                    when {
                        this == null || this.bookId != mediaId -> {
                            player.setBook(cc, book, startAt)
                            currentBook = book
                        }
                        else ->
                            if (player.chapter != cc) {
                                player.setBook(
                                    cc,
                                    book,
                                    startAt
                                )
                            }

                    }
                    startPlaying()
                }
            }
        }

        override fun onPlay() {
            startPlaying()
        }

        override fun onPause() {
            pausePlaying()
        }

        override fun onStop() {
            stopPlaying()
        }

        override fun onRewind() {
            player.move(-10000)
        }

        override fun onFastForward() {
            player.move(10000)
        }

        fun pausePlaying() {
            player.pause()
        }


        override fun onSeekTo(pos: Long) {
            player.seekTo(pos.toInt())
        }

        override fun onSkipToNext() {
            val nextChapter = player.chapter + 1
            if (nextChapter < currentBook!!.book.chapNum) {
                selectChapter(nextChapter)
            }
        }

        override fun onSkipToPrevious() {
            val nextChapter = player.chapter - 1
            if (nextChapter < currentBook!!.book.chapNum) {
                selectChapter(nextChapter)
            }
        }

        private fun selectChapter(chapterNum: Int) {
            with(currentBook) {
                val chapter = this!!.chapters[chapterNum]
                player.setBook(
                    chapterNum,
                    this,
                    if (chapter.len == chapter.listened) 0 else chapter.listened
                )
                startPlaying()
            }
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            val moveVal = when (action) {
                REPLAY_10_VAL -> -10000
                REPLAY_30_VAL -> -30000
                FORWARD_10_VAL -> 10000
                FORWARD_30_VAL -> 30000
                else -> 0
            }
            if (moveVal != 0) {
                player.move(moveVal)
            }
        }

        private fun startPlaying() {
            player.register(this)
            player.play()
        }

        private fun stopPlaying() {
            player.stop()
            currentBook = null
            noisyObserver.unRegister()
            mediaSession.isActive = false
            stopForeground(true)
            stopSelf()
            player.unregister(this)
        }

        override fun bookPlaying() {
            mediaSession.isActive = true
            val notification = notifBuilder.buildNotification(mediaSession.sessionToken)
            notificationManager.notify(NOTIFICATION_ID, notification)
            noisyObserver.register()
            if (!isForeground) {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, this@PlayerService.javaClass)
                )
                startForeground(NOTIFICATION_ID, notification)
                isForeground = true
            }
        }

        override fun bookPaused() {
            stopForeground(false)
            isForeground = false
            val notification = notifBuilder.buildNotification(mediaSession.sessionToken)
            notificationManager.notify(NOTIFICATION_ID, notification)
            noisyObserver.unRegister()
        }


        override fun bookFinished() {
            stopPlaying()
        }

    }


    private inner class NoisyObserver(
        private val context: Context,
        token: MediaSessionCompat.Token
    ) : BroadcastReceiver() {

        private val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        private var registered = false
        private val controller = MediaControllerCompat(context, token)

        fun register() {
            if (!registered) {
                context.registerReceiver(this, noisyIntentFilter)
            }
        }

        fun unRegister() {
            if (registered) {
                context.unregisterReceiver(this)
            }
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                controller.transportControls.pause()
            }
        }

    }

}

const val REPLAY_10_VAL = "replay_10"
const val REPLAY_30_VAL = "replay_30"
const val FORWARD_10_VAL = "forward_10"
const val FORWARD_30_VAL = "forward_30"
private const val TAG = "FLAUBOOK"
private const val NOTIFICATION_ID = 1
