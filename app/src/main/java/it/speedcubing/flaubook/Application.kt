package it.speedcubing.flaubook

import android.app.Application
import it.speedcubing.flaubook.database.BookRepository
import it.speedcubing.flaubook.tools.ThemeManager

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        ThemeManager.initialize(this)
        ThemeManager.loadDefaultTheme()
        BookRepository.initialize(this)
    }

}