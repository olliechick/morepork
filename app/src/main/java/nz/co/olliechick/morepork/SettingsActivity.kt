package nz.co.olliechick.morepork

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
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

        private val REQUEST_RECORD_AUDIO_SETTINGS_PERMISSION = 441
        private var soundMeter: SoundMeter? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            initSettings()

            // Register alert dialog for when "Test sound level" is tapped
            findPreference<Preference>("testSoundLevelButton")?.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    // custom dialog
                    if (context == null) false
                    else {
                        if (Util.hasAudioPermission(context!!)) showSoundLevelDialog()
                        else Util.getAudioPermission(this, REQUEST_RECORD_AUDIO_SETTINGS_PERMISSION)
                        true
                    }
                }
        }

        private fun showSoundLevelDialog() {
            var alertDialog: AlertDialog? = null
            soundMeter = SoundMeter()
            soundMeter?.start()

            alertDialog = AlertDialog.Builder(context!!).apply {
                setTitle("Current sound level")
                setMessage("Loading...")
                setNegativeButton("OK") { _, _ -> soundMeter?.stop() }
            }.run { show() }

            createSoundLevelHandler(alertDialog)
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == REQUEST_RECORD_AUDIO_SETTINGS_PERMISSION) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) showSoundLevelDialog()
                else Toast.makeText(
                    activity,
                    "You will need to allow the app to record audio to test your sound level.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        private fun createSoundLevelHandler(alertDialog: AlertDialog?) {
            val handler = Handler()
            handler.postDelayed(object : Runnable {
                override fun run() {
                    if (soundMeter != null) alertDialog?.setMessage(soundMeter!!.amplitude.toInt().toString())
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