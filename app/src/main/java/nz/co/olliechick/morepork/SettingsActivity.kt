package nz.co.olliechick.morepork

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import nz.co.olliechick.morepork.Util.Companion.DELAY


class SettingsActivity : AppCompatActivity() {

    private val listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == "theme") {
                Util.updateTheme(prefs)
                recreate()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(listener)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            initSettings()

            // Register alert dialog for when "Test sound level" is tapped
            var alertDialog: AlertDialog? = null
            findPreference<Preference>("testSoundLevelButton")?.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    // custom dialog
                    if (context == null) false
                    else {
                        alertDialog = AlertDialog.Builder(context!!).apply {
                            setTitle("Current sound level")
                            setMessage("Loading...")
                            setNegativeButton("OK", null)
                        }.run { show() }
                        true
                    }
                }

            // Create handler for updating sound level
            val handler = Handler()
            val soundMeter = SoundMeter().apply { start() }
            handler.postDelayed(object : Runnable {
                override fun run() {
                    alertDialog?.setMessage(soundMeter.amplitude.toInt().toString())
                    handler.postDelayed(this, DELAY)
                }
            }, DELAY)
        }

        private fun initSettings() {
            findPreference<Preference>("feedback")?.setOnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:" + getString(R.string.dev_email))
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                startActivity(intent)
                true
            }
        }
    }
}