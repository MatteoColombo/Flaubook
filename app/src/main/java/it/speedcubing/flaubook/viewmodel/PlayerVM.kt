package it.speedcubing.flaubook.viewmodel

import android.media.session.PlaybackState
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import it.speedcubing.flaubook.connection.ConnectionAction
import it.speedcubing.flaubook.connection.PlayerConnectionManager

class PlayerVM(private val connection: PlayerConnectionManager) : ViewModel() {

    private var isConnected = false
    val isPlayPause = MutableLiveData<Boolean>().apply { false }
    private val connectionObserver = Observer<Boolean> { isConnected = it }
    private val stateObserver = Observer<PlaybackStateCompat> {
        it?.run {
            isPlayPause.postValue(
                when (it.state) {
                    PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_PAUSED -> true
                    else -> false
                }
            )
        }
    }


    fun connect() {
        connection.connect()
        connection.isConnected.observeForever(connectionObserver)
        connection.playState.observeForever(stateObserver)
    }

    fun disconnect() {
        connection.isConnected.removeObserver(connectionObserver)
        connection.playState.removeObserver(stateObserver)
        connection.disconnect()
    }


    fun playSomething(id: String, chapter: Int = -1) {
        connection.sendCommand(ConnectionAction.PLAY_BOOK, id, chapter)
    }


    class Factory(
        private val mediaServiceConnection: PlayerConnectionManager
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PlayerVM(mediaServiceConnection) as T
        }
    }


}