package it.speedcubing.flaubook

import android.content.ComponentName
import android.content.Context
import it.speedcubing.flaubook.connection.PlayerConnectionManager
import it.speedcubing.flaubook.service.PlayerService
import it.speedcubing.flaubook.viewmodel.MainVM

object Injector {

    private fun providePlayerConnection(context: Context): PlayerConnectionManager {
        return PlayerConnectionManager.getInstance(
            context,
            ComponentName(context, PlayerService::class.java)
        )
    }

    fun provideMainViewModel(context: Context): MainVM.Factory {
        val connection = providePlayerConnection(context.applicationContext)
        return MainVM.Factory(connection)
    }

}