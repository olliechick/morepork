package nz.co.olliechick.morepork

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ShareCompat

class GameOverActivity : AppCompatActivity() {
    private val PREFS_NAME = "scores"
    private val HIGH_SCORE = "highscore"
    private val PLAY_STORE_ADDRESS = "https://play.google.com/store/apps/details?id=nz.co.olliechick.morepork"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        val sharedPref: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // get reference to buttons
        val playButton = findViewById<Button>(R.id.play_again_button)
        val homeButton = findViewById<Button>(R.id.home_button)
        val shareButton = findViewById<Button>(R.id.share_button)

        val score = intent.getIntExtra("score", 0)
        var yourScore = findViewById<TextView>(R.id.your_score_textview).setText(score.toString())
        var highScoreTextView = findViewById<TextView>(R.id.high_score_textview)

        // we check if the new score is a high score and if so update highscore
        var highscore = sharedPref.getInt(HIGH_SCORE, 0)
        if (score > highscore){
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putInt(HIGH_SCORE, score)
            editor.commit()
            highScoreTextView.setText(score.toString())
        } else {
            highScoreTextView.setText(highscore.toString())
        }

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

        // Allows the user to share their score and a link to the app
        shareButton.setOnClickListener { view ->
            val shareIntent = ShareCompat.IntentBuilder
                .from(this)
                .setType("text/plain")
                .setChooserTitle("Share")
                .setText("I got a high score of $score on Morepork. Can you beat it?\n $PLAY_STORE_ADDRESS")
                .intent

            if (shareIntent.resolveActivity(this.packageManager) != null) {
                this.startActivity(shareIntent)
            }
        }
    }

}
