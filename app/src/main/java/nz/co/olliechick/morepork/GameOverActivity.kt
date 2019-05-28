package nz.co.olliechick.morepork

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class GameOverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        // get reference to buttons
        val playButton = findViewById<Button>(R.id.play_again_button)
        val homeButton = findViewById<Button>(R.id.home_button)

        val score = intent.getIntExtra("score", 0)
        var yourScore = findViewById<TextView>(R.id.your_score_textview).setText(score.toString())

        // set on-click listeners
        playButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
            finish()
        }
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


}
