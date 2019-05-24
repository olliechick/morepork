package nz.co.olliechick.morepork

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class Util {
    companion object {
        const val helpUrl =
            "https://docs.google.com/document/d/1p9o2-fDhx2gYimRF4TkAfh5I9CPeMrvjpM7GsClGvqA/edit?usp=sharing"

        fun updateTheme(prefs: SharedPreferences): String {
            val themePref = prefs.getString("theme", "")
            return if (themePref == "dark") {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                "dark"
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "light"
            }
        }
    }
}