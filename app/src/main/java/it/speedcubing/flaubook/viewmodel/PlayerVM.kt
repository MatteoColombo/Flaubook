package it.speedcubing.flaubook.viewmodel

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import it.speedcubing.flaubook.connection.ConnectionAction
import it.speedcubing.flaubook.connection.PlayerConnectionManager

class PlayerVM(private val connection: PlayerConnectionManager) : ViewModel() {

    private var isConnected = false

    private val connectionObserver = Observer<Boolean> { isConnected = it }


    fun connect() {
        connection.connect()
        connection.isConnected.observeForever(connectionObserver)
    }

    fun disconnect() {
        connection.isConnected.removeObserver(connectionObserver)
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