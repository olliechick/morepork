package nz.co.olliechick.morepork

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.app.ActivityCompat

class Util {
    companion object {
        private const val CHECK_FREQUENCY = 10 // times per second the audio level is sampled
        const val DELAY = (1000 / CHECK_FREQUENCY).toLong() //milliseconds

        fun updateTheme(prefs: SharedPreferences) {
            when (prefs.getString("theme", "light")) {
                "dark" -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                "light" -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            }
        }

        fun hasAudioPermission(context: Context): Boolean {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        }

        fun getAudioPermission(context: Activity, requestCode: Int) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                requestCode
            )
        }

        fun getAudioPermission(context: SettingsActivity.SettingsFragment, requestCode: Int) {
            context.requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), requestCode)
        }

        fun getStringFromDouble(double: Double): String {
            return if (double.rem(1.0) == 0.0) "${double.toInt()}"
            else "$double"
        }


    }
}