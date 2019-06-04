package nz.co.olliechick.morepork

import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.net.Uri.parse
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager


open class MainActivity : AppCompatActivity() {

    private val REQUEST_RECORD_AUDIO_GAME_PERMISSION = 440
    private var mApplyNightMode = false

    private var listener: OnSharedPreferenceChangeListener =
        OnSharedPreferenceChangeListener { _, key ->
            if (key == "theme") mApplyNightMode = true
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(listener)
        Util.updateTheme(PreferenceManager.getDefaultSharedPreferences(this))

        // get reference to buttons
        val playButton = findViewById<Button>(R.id.play_button)
        val helpButton = findViewById<Button>(R.id.help_button)

        // set on-click listeners
        playButton.setOnClickListener {
            if (Util.hasAudioPermission(this)) launchGame()
            else Util.getAudioPermission(this, REQUEST_RECORD_AUDIO_GAME_PERMISSION)
        }

        helpButton.setOnClickListener {
            val uri = parse(getString(R.string.help_url))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        val actionBar = supportActionBar
        actionBar?.title = getString(R.string.app_name)
    }

    override fun onResume() {
        super.onResume()
        if (mApplyNightMode) {
            mApplyNightMode = false
            recreate()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_GAME_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) launchGame()
            else Toast.makeText(
                this,
                R.string.allow_app_audio_for_game,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun launchGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.mainmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            true
        }

        else -> super.onOptionsItemSelected(item)
    }
}
