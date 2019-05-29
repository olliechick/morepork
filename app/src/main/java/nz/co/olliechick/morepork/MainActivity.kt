package nz.co.olliechick.morepork

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri.*
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager


class MainActivity : AppCompatActivity() {

    private val REQUEST_RECORD_AUDIO_PERMISSION = 440
    private var displayedTheme: String = "light"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get reference to buttons
        val playButton = findViewById<Button>(R.id.play_button)
        val helpButton = findViewById<Button>(R.id.help_button)

        // set on-click listeners
        playButton.setOnClickListener {
            Toast.makeText(this, "Clicked play", Toast.LENGTH_SHORT).show();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_PERMISSION
                )
            } else launchGame()

        }

        helpButton.setOnClickListener {
            val uri = parse(Util.helpUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        val actionBar = supportActionBar
        actionBar?.title = getString(R.string.app_name)

        displayedTheme = Util.updateTheme(PreferenceManager.getDefaultSharedPreferences(this))
    }

//    override fun onResume() {
//        super.onResume()
//        val currentTheme = PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "")
//        if (currentTheme != displayedTheme) recreate()
//    }
//
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchGame()
        } else {
            Toast.makeText(this, "You will need to allow the app to record audio to play the game.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun launchGame() {
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
