package it.speedcubing.flaubook.service

import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import it.speedcubing.flaubook.R
import it.speedcubing.flaubook.connection.NOTHING_PLAYING

class SessionManager(private val session: MediaSessionCompat) {

    private val pbsBuilder = PlaybackStateCompat.Builder()


    fun updateMeta(book: ServiceBook?, chapter: Int = 0) {
        session.setMetadata(
            when (book) {
                null -> NOTHING_PLAYING
                else -> book.getMetadata(chapter)
            }
        )
    }

    fun updateState(state: Int, position: Long = 0) {
        generateActions(state)
        setState(state, position)
        session.setPlaybackState(pbsBuilder.build())
    }

    private fun setState(state: Int, position: Long = 0) {
        pbsBuilder.setState(
            state,
            position,
            if (state == PlaybackStateCompat.STATE_PLAYING) 1f else 0f
        )
    }

    private fun generateActions(state: Int) {
        var action: Long =
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or PlaybackStateCompat.ACTION_STOP
        when (state) {
            PlaybackStateCompat.STATE_PLAYING -> action =
                action or PlaybackStateCompat.ACTION_PAUSE
            PlaybackStateCompat.STATE_PAUSED -> action =
                action or PlaybackStateCompat.ACTION_PLAY
            else -> false
        }
        when (state) {
            PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_PAUSED -> {
                action = action or PlaybackStateCompat.ACTION_SEEK_TO
                val meta = session.controller.metadata
                when (meta.trackNumber) {
                    meta.trackCount -> {
                    }
                    else -> action = action or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                }
                when (meta.trackNumber) {
                    0L -> {
                    }
                    else -> action = action or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                }
                action =
                    action or PlaybackStateCompat.ACTION_FAST_FORWARD or PlaybackStateCompat.ACTION_REWIND
                pbsBuilder.addCustomAction(ACTION_FW_10)
                pbsBuilder.addCustomAction(ACTION_BW_10)
                pbsBuilder.addCustomAction(ACTION_FW_30)
                pbsBuilder.addCustomAction(ACTION_BW_30)
            }
            else -> false
        }
        pbsBuilder.setActions(action)
    }


    val ACTION_FW_10 = PlaybackStateCompat.CustomAction.Builder(
        FORWARD_10_VAL,
        "Avanza 10 secondi",
        R.drawable.forward_10
    ).build()

    val ACTION_FW_30 = PlaybackStateCompat.CustomAction.Builder(
        FORWARD_30_VAL,
        "Avanza 30 secondi",
        R.drawable.ic_forward_30_black_48dp
    ).build()

    val ACTION_BW_10 = PlaybackStateCompat.CustomAction.Builder(
        REPLAY_10_VAL,
        "Riavvolgi 10 secondi",
        R.drawable.replay_10
    ).build()

    val ACTION_BW_30 = PlaybackStateCompat.CustomAction.Builder(
        REPLAY_30_VAL,
        "Riavvolgi 30 secondi",
        R.drawable.ic_replay_30_black_48dp
    ).build()

}