package it.speedcubing.flaubook.connection

import android.content.ComponentName
import android.content.Context
import android.media.session.PlaybackState
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import it.speedcubing.flaubook.service.FORWARD_30_VAL
import it.speedcubing.flaubook.service.REPLAY_30_VAL
import it.speedcubing.flaubook.service.id
import java.util.concurrent.Executors

class PlayerConnectionManager(context: Context, componentName: ComponentName) {

    val executor = Executors.newSingleThreadExecutor()
    val isConnected = MutableLiveData<Boolean>()
    val playState = MutableLiveData<PlaybackStateCompat>().apply { postValue(EMPTY_PLAYBACK_STATE) }
    val nowPlaying = MutableLiveData<MediaMetadataCompat>().apply { postValue(NOTHING_PLAYING) }

    private val tpc: MediaControllerCompat.TransportControls get() = controller.transportControls
    private var latestID: String? = null

    private lateinit var browser: MediaBrowserCompat
    private lateinit var controller: MediaControllerCompat
    private val callback = object : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            super.onConnected()
            controller = MediaControllerCompat(context, browser.sessionToken)
            controller.registerCallback(ControllerCallback())
            isConnected.postValue(true)
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            isConnected.postValue(false)
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            isConnected.postValue(false)
        }

    }


    init {
        isConnected.postValue(false)
        browser = MediaBrowserCompat(context, componentName, callback, null)
    }

    fun connect() {
        when (browser.isConnected) {
            false -> browser.connect()
            true -> Log.i("CONNECTION", "Browser already connected, skipping")
        }
    }

    fun disconnect() {
        when (browser.isConnected) {
            true -> browser.disconnect()
            false -> Log.i("CONNECTION", "Browser already disconnected, skipping")
        }
    }


    fun sendCommand(action: ConnectionAction, id: String = "", extra: Int = -1) = executor.execute {
        if (browser.isConnected) {
            val bundle = Bundle()
            bundle.putInt("chapter_id", extra)
            when (action) {
                ConnectionAction.PLAY_PAUSE -> {
                    when (playState.value!!.state) {
                        PlaybackStateCompat.STATE_PLAYING -> tpc.pause()
                        PlaybackStateCompat.STATE_PAUSED, PlaybackState.STATE_NONE -> handlePlay(
                            bundle
                        )
                        else -> {
                        }
                    }
                }
                ConnectionAction.PLAY_BOOK, ConnectionAction.PLAY_CHAPTER -> tpc.playFromMediaId(
                    id,
                    bundle
                )
                ConnectionAction.PLAY -> handlePlay(bundle)
                else -> {
                    when (playState.value!!.state) {
                        PlaybackState.STATE_PAUSED, PlaybackState.STATE_PLAYING -> {
                            when (action) {
                                ConnectionAction.PAUSE -> tpc.pause()
                                ConnectionAction.SKIP_NEXT -> tpc.skipToNext()
                                ConnectionAction.SKIP_PREV -> tpc.skipToPrevious()
                                ConnectionAction.MOVE_FW -> tpc.sendCustomAction(
                                    FORWARD_30_VAL,
                                    null
                                )
                                ConnectionAction.MOVE_BW -> tpc.sendCustomAction(
                                    REPLAY_30_VAL,
                                    null
                                )
                                ConnectionAction.SEEK_TO -> tpc.seekTo(extra.toLong())
                                else -> {
                                }
                            }
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }


    private fun handlePlay(bundle: Bundle) {
        with(playState.value!!.state) {
            when (this) {
                PlaybackStateCompat.STATE_NONE -> latestID?.run {
                    tpc.playFromMediaId(
                        this,
                        bundle
                    )
                }
                PlaybackStateCompat.STATE_PAUSED -> tpc.play()
                else -> false
            }
        }
    }

    private inner class ControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playState.postValue(state ?: EMPTY_PLAYBACK_STATE)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            val meta = metadata ?: NOTHING_PLAYING
            nowPlaying.postValue(meta)
            if (meta.id != "") {
                latestID = meta.id.toString()
            }
        }

    }

    companion object {
        @Volatile
        private var instance: PlayerConnectionManager? = null

        fun getInstance(context: Context, serviceComponent: ComponentName) =
            instance ?: synchronized(this) {
                instance ?: PlayerConnectionManager(context, serviceComponent)
                    .also { instance = it }
            }
    }
}

@Suppress("PropertyName")
val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()

@Suppress("PropertyName")
val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
    .build()