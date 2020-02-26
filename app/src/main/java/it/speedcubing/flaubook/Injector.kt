package it.speedcubing.flaubook

import android.content.ComponentName
import android.content.Context
import it.speedcubing.flaubook.connection.PlayerConnectionManager
import it.speedcubing.flaubook.service.PlayerService
import it.speedcubing.flaubook.viewmodel.BookVM
import it.speedcubing.flaubook.viewmodel.PlayerVM

object Injector {

    private fun providePlayerConnection(context: Context): PlayerConnectionManager {
        return PlayerConnectionManager.getInstance(
            context,
            ComponentName(context, PlayerService::class.java)
        )
    }

    fun providePlayerModel(context: Context): PlayerVM.Factory {
        val connection = providePlayerConnection(context.applicationContext)
        return PlayerVM.Factory(connection)
    }

    fun provideBookModel(context: Context): BookVM.Factory{
        val connection = providePlayerConnection(context.applicationContext)
        return BookVM.Factory(connection)
    }

}