package me.olliechick.stilluntitled

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get reference to buttons
        val playButton = findViewById(R.id.play_button) as Button
        val helpButton = findViewById(R.id.help_button) as Button

        // set on-click listeners
        playButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "TODO: Load game instance.", Toast.LENGTH_SHORT).show()
        }
        helpButton.setOnClickListener {
            Toast.makeText(this@MainActivity, "TODO: Load help.", Toast.LENGTH_SHORT).show()
        }
    }
}
