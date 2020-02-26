package it.speedcubing.flaubook.tools

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class ThemeManager {

    companion object {

        private var appContext: Context? = null

        fun initialize(context: Context) {
            appContext = context
        }

        fun loadDefaultTheme() {
            val pm = PreferenceManager.getDefaultSharedPreferences(appContext)
            AppCompatDelegate.setDefaultNightMode(
                when (pm.getInt("theme_pref", AppCompatDelegate.MODE_NIGHT_YES)) {
                    AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_NO
                    AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_YES
                }
            )
        }

        fun setTheme(theme: Int) {
            AppCompatDelegate.setDefaultNightMode(theme)
            val pm = PreferenceManager.getDefaultSharedPreferences(appContext)
            with(pm.edit()) {
                putInt("theme_pref", theme)
                commit()
            }
        }
    }

}