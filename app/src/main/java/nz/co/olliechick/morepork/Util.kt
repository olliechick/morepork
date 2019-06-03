package nz.co.olliechick.morepork

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES

class Util {
    companion object {
        const val helpUrl =
            "https://docs.google.com/document/d/1p9o2-fDhx2gYimRF4TkAfh5I9CPeMrvjpM7GsClGvqA/edit?usp=sharing"

        fun updateTheme(prefs: SharedPreferences) {
            when (prefs.getString("theme", "light")) {
                "dark" -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                "light" -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            }
        }
    }
}