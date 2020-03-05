package it.speedcubing.flaubook.viewmodel

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import it.speedcubing.flaubook.R
import it.speedcubing.flaubook.connection.ConnectionAction
import it.speedcubing.flaubook.connection.EMPTY_PLAYBACK_STATE
import it.speedcubing.flaubook.connection.NOTHING_PLAYING
import it.speedcubing.flaubook.connection.PlayerConnectionManager
import it.speedcubing.flaubook.database.Book
import it.speedcubing.flaubook.database.BookRepository
import it.speedcubing.flaubook.service.*

class MainVM(private val connection: PlayerConnectionManager) : ViewModel() {
    private val bookRepository = BookRepository.get()
    val bookListLD = bookRepository.getBooks()

    fun deleteBook(book: Book) = bookRepository.deleteBook(book)
    fun resetBook(book: Book) = bookRepository.resetBook(book)
    fun finishBook(book: Book) = bookRepository.markBookAsFinished(book)

    private var isConnected = false
    private var state: PlaybackStateCompat = EMPTY_PLAYBACK_STATE
    private var updatePosition = true
    private val handler = Handler(Looper.getMainLooper())


    val isPlayPause = MutableLiveData<Boolean>().apply { postValue(false) }
    val meta = MutableLiveData<NowPlayingMetadata>()
    val position = MutableLiveData<Long>().apply { postValue(0L) }
    val playPauseRes =
        MutableLiveData<Int>().apply { postValue(R.drawable.ic_pause_circle_filled_black_78dp) }
    val playPauseResMini =
        MutableLiveData<Int>().apply { postValue(R.drawable.notification_pause) }

    private val connectionObserver = Observer<Boolean> { isConnected = it }
    private val pbsObserver = Observer<PlaybackStateCompat> {
        state = it ?: EMPTY_PLAYBACK_STATE
        val metadata = connection.nowPlaying.value ?: NOTHING_PLAYING
        updateState(state, metadata)
        isPlayPause.postValue(
            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_PAUSED -> true
                else -> false
            }
        )
    }

    private val mmObserver = Observer<MediaMetadataCompat> {
        updateState(state, it)
    }


    init {
        connection.isConnected.observeForever(connectionObserver)
        connection.nowPlaying.observeForever(mmObserver)
        connection.playState.observeForever(pbsObserver)
        updatePosition = true
        checkPlaybackPosition()
    }

    override fun onCleared() {
        super.onCleared()
        connection.isConnected.removeObserver(connectionObserver)
        connection.playState.removeObserver(pbsObserver)
        connection.nowPlaying.removeObserver(mmObserver)
        updatePosition = false
    }

    fun playSomething(id: String, chapter: Int = -1) {
        connection.sendCommand(ConnectionAction.PLAY_BOOK, id, chapter)
    }

    fun sendAction(action: ConnectionAction) {
        connection.sendCommand(action)
    }

    fun seekTo(moveTo: Int) {
        connection.sendCommand(ConnectionAction.SEEK_TO, extra = moveTo)
    }


    private fun checkPlaybackPosition(): Boolean = handler.postDelayed({
        val currPosition = state.currentPlayBackPosition
        if (position.value != currPosition)
            position.postValue(currPosition)
        if (updatePosition)
            checkPlaybackPosition()
    }, 1000)


    private fun updateState(
        playbackState: PlaybackStateCompat,
        mediaMetadata: MediaMetadataCompat
    ) {
        if (mediaMetadata.duration != 0L) {
            val nowPlayingMetadata = NowPlayingMetadata(
                mediaMetadata.id,
                mediaMetadata.album,
                mediaMetadata.description.iconBitmap,
                mediaMetadata.title,
                mediaMetadata.duration.toInt(),
                mediaMetadata.trackNumber.toInt(),
                mediaMetadata.trackNumber == mediaMetadata.trackCount,
                mediaMetadata.trackNumber == 1L
            )
            this.meta.postValue(nowPlayingMetadata)
        }

        playPauseRes.postValue(
            when (playbackState.isPlaying) {
                true -> R.drawable.ic_pause_circle_filled_black_78dp
                else -> R.drawable.ic_play_circle_filled_black_78dp
            }
        )
        playPauseResMini.postValue(
            when (playbackState.isPlaying) {
                true -> R.drawable.notification_pause
                else -> R.drawable.notification_play
            }
        )
    }


    data class NowPlayingMetadata(
        val id: String,
        val book: String?,
        val image: Bitmap?,
        val title: String?,
        val duration: Int?,
        val chapter: Int?,
        val isLast: Boolean,
        val isFirst: Boolean
    )

    fun connect(){
        connection.connect()
    }

    fun disconnect(){
        connection.disconnect()
    }


    class Factory(
        private val mediaServiceConnection: PlayerConnectionManager
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainVM(mediaServiceConnection) as T
        }
    }


}