package nz.co.olliechick.morepork

import android.content.SharedPreferences
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES

class Util {
    companion object {
        const val helpUrl =
            "https://docs.google.com/document/d/1p9o2-fDhx2gYimRF4TkAfh5I9CPeMrvjpM7GsClGvqA/edit?usp=sharing"

        /**
         * If the theme needs updating, does it.
         * If currentNightMode is different to the new theme, returns true
         * Else returns false
         */
        fun updateTheme(prefs: SharedPreferences, currentNightMode: Int): Boolean {
            return when (prefs.getString("theme", "light")) {
                "dark" -> {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                    currentNightMode != UI_MODE_NIGHT_YES
                }
                "light" -> {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                    currentNightMode != UI_MODE_NIGHT_NO
                }
                else -> false
            }
        }

        /*
         * Updates the theme regardless of whether it needs it
         */
        fun updateTheme(prefs: SharedPreferences) {
            updateTheme(prefs, 0)
        }


    }
}