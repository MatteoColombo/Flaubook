package it.speedcubing.flaubook.service

import android.content.Context
import android.media.MediaPlayer
import android.media.session.PlaybackState
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import it.speedcubing.flaubook.database.BookRepository

class FlaubookPlayer(context: Context, private val session: MediaSessionCompat) :
    FocusManager.FocusCallback {

    private var player: MediaPlayer
    private val repo = BookRepository.get()
    private lateinit var book: ServiceBook
    private var currentChapter = 0
    private var observers = mutableSetOf<PlayerCallback>()
    private val focusManager = FocusManager(context, this)
    private val sessionManager = SessionManager(session)

    init {
        player = MediaPlayer()
        sessionManager.updateMeta(null)
        sessionManager.updateState(PlaybackStateCompat.STATE_NONE)
    }

    val chapter get() = currentChapter


    fun play() = focusManager.requestFocusAndPlay()

    fun pause() {
        focusManager.abandonFocus()
    }

    fun stop() {
        if (player.isPlaying)
            focusManager.abandonFocus()
        player.release()
        sessionManager.updateMeta(null)
        sessionManager.updateState(PlaybackStateCompat.STATE_NONE)
    }

    fun move(delta: Int) {
        val newPosition = player.currentPosition + delta
        seekTo(newPosition)
    }

    fun seekTo(seekTo: Int) {
        val newPosition = when {
            seekTo < 0 -> 0
            seekTo >= player.duration -> player.duration - 1000
            else -> seekTo
        }.toLong()
        player.seekTo(newPosition, MediaPlayer.SEEK_CLOSEST)
        updateStatePositionNoActions()
        saveProgress()
    }

    private fun updateStatePosition() {
        sessionManager.updateState(
            session.controller.playbackState.state,
            player.currentPosition.toLong()
        )
    }

    private fun updateStatePositionNoActions() {
        sessionManager.updateStateNoActions(
            session.controller.playbackState.state,
            player.currentPosition.toLong()
        )
    }

    fun setBook(chapter: Int, book: ServiceBook, startAt: Int = 0) {
        if (session.controller.playbackState.state == PlaybackState.STATE_NONE) {
            player = MediaPlayer()
        }
        if (player.isPlaying) {
            saveProgress()
        }
        this.book = book
        setChapter(chapter, startAt)
    }

    private fun setChapter(chapter: Int, startAt: Int) {
        this.currentChapter = chapter
        preparePlayer(book.chapters[chapter].file, startAt)
        repo.selectChapter(book.chapters[chapter], startAt)
    }


    private fun preparePlayer(path: String, startAt: Int) {
        player.reset()
        player.setDataSource(path)
        player.prepare()
        player.seekTo(startAt)
        player.setOnCompletionListener {
            with(book) {
                val chapter = this.chapters[currentChapter]
                if ((chapter.chapId + 1) == this.chapters.size) {
                    repo.markBookAsFinished(this.book)
                    focusManager.abandonFocus()
                    observers.forEach { it.bookFinished() }
                } else {
                    repo.moveToNextChapter(chapter)
                    currentChapter += 1
                    preparePlayer(this.chapters[currentChapter].file, 0)
                    focusManager.requestFocusAndPlay()
                }

            }
        }

        player.setOnSeekCompleteListener { updateStatePosition() }
    }


    private fun updateMetaAndPlay() {
        sessionManager.updateMeta(book, currentChapter)
        player.start()
        sessionManager.updateState(PlaybackState.STATE_PLAYING, player.currentPosition.toLong())
    }

    override fun focusPlay() {
        updateMetaAndPlay()
        observers.forEach { it.bookPlaying() }
    }

    override fun focusPause() {
        if (player.isPlaying) {
            player.pause()
            sessionManager.updateState(PlaybackState.STATE_PAUSED, player.currentPosition.toLong())
            saveProgress()
            observers.forEach { it.bookPaused() }
        } else {
            sessionManager.updateMeta(null)
            sessionManager.updateState(PlaybackState.STATE_NONE)
        }
    }

    private fun saveProgress() {
        val chapter = book.chapters[currentChapter]
        repo.saveChapterProgress(chapter, player.currentPosition - 3000)
    }


    fun register(observer: PlayerCallback) {
        observers.add(observer)
    }

    fun unregister(observer: PlayerCallback) {
        observers.remove(observer)
    }


    interface PlayerCallback {
        fun bookPlaying()
        fun bookPaused()
        fun bookFinished()
    }

}