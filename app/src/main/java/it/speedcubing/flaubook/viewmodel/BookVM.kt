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
import it.speedcubing.flaubook.service.*

class BookVM(private val connection: PlayerConnectionManager) : ViewModel() {


    private var state: PlaybackStateCompat = EMPTY_PLAYBACK_STATE
    val meta = MutableLiveData<NowPlayingMetadata>()
    val position = MutableLiveData<Long>().apply { postValue(0L) }
    val playPauseRes =
        MutableLiveData<Int>().apply { postValue(R.drawable.ic_pause_circle_filled_black_78dp) }
    val playPauseResMini =
        MutableLiveData<Int>().apply { postValue(R.drawable.ic_pause_black_48dp) }
    private var updatePosition = true
    private val handler = Handler(Looper.getMainLooper())


    fun sendAction(action: ConnectionAction) {
        connection.sendCommand(action)
    }

    fun seekTo(moveTo: Int) {
        connection.sendCommand(ConnectionAction.SEEK_TO, extra = moveTo)
    }

    private val pbsObserver = Observer<PlaybackStateCompat> {
        state = it ?: EMPTY_PLAYBACK_STATE
        val metadata = connection.nowPlaying.value ?: NOTHING_PLAYING
        updateState(state, metadata)
    }

    private val mmObserver = Observer<MediaMetadataCompat> {
        updateState(state, it)
    }

    private val musicServiceConnection = connection.also {
        it.playState.observeForever(pbsObserver)
        it.nowPlaying.observeForever(mmObserver)
        checkPlaybackPosition()
    }

    private fun checkPlaybackPosition(): Boolean = handler.postDelayed({
        val currPosition = state.currentPlayBackPosition
        if (position.value != currPosition)
            position.postValue(currPosition)
        if (updatePosition)
            checkPlaybackPosition()
    }, 1000)


    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.playState.removeObserver(pbsObserver)
        musicServiceConnection.nowPlaying.removeObserver(mmObserver)
        updatePosition = false
    }


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
                true -> R.drawable.ic_pause_black_48dp
                else -> R.drawable.ic_play_arrow_black_48dp
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


    class Factory(
        private val mediaServiceConnection: PlayerConnectionManager
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return BookVM(mediaServiceConnection) as T
        }
    }
}