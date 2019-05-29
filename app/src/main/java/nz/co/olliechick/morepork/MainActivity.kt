package nz.co.olliechick.morepork

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.preference.PreferenceManager


class MainActivity : AppCompatActivity() {

    private var displayedTheme: String = "light"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get reference to buttons
        val playButton = findViewById<Button>(R.id.play_button)
        val helpButton = findViewById<Button>(R.id.help_button)

        // set on-click listeners
        playButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
            finish()
        }
        helpButton.setOnClickListener {
            val uri = Uri.parse(Util.helpUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
            finish()
        }

        val actionBar = supportActionBar
        actionBar?.title = getString(R.string.app_name)

        displayedTheme = Util.updateTheme(PreferenceManager.getDefaultSharedPreferences(this))
    }

    override fun onResume() {
        super.onResume()
        val currentTheme = PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "")
        if (currentTheme != displayedTheme) recreate()
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
            finish()
            true
        }

        else -> super.onOptionsItemSelected(item)

    }
}
