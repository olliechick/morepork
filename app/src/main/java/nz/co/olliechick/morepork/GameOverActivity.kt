package nz.co.olliechick.morepork

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import kotlinx.android.synthetic.main.activity_game_over.*

class GameOverActivity : AppCompatActivity() {
    private val PREFS_NAME = "scores"
    private val HIGH_SCORE = "highscore"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        val sharedPref: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val score = intent.getDoubleExtra("score", 0.0)
        val highScore = java.lang.Double.longBitsToDouble(sharedPref.getLong(HIGH_SCORE, 0))

        your_score_label.text = getString(R.string.your_score, Util.getStringFromDouble(score))

        // we check if the new score is a high score and if so update high score
        if (score > highScore) {
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.putLong(HIGH_SCORE, java.lang.Double.doubleToRawLongBits(score))
            editor.apply()
            high_score_label.text = getString(R.string.high_score, Util.getStringFromDouble(score))
        } else {
            high_score_label.text = getString(R.string.high_score, Util.getStringFromDouble(highScore))
        }


        // set on-click listeners
        play_again_button.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
            finish()
        }
        home_button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Allows the user to share their score and a link to the app
        share_button.setOnClickListener { _ ->
            val shareIntent = ShareCompat.IntentBuilder
                .from(this)
                .setType("text/plain")
                .setChooserTitle(R.string.share_score)
                .setText(
                    getString(R.string.share_text, score, getString(R.string.app_name)) + "\n\n" +
                            getString(R.string.play_store_url)
                )
                .intent

            if (shareIntent.resolveActivity(this.packageManager) != null) {
                this.startActivity(shareIntent)
            }
        }
    }
}
