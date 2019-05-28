package nz.co.olliechick.morepork

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.os.Handler
import androidx.core.app.ActivityCompat
import android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
import android.graphics.Point
import android.view.Gravity
import android.view.WindowManager


class GameActivity : AppCompatActivity(), SensorEventListener {


    private var sideScrollView: SideScrollView? = null

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val SOUND_BARRIER = 2000 // how loud it has to be to move the avatar up
    private val CHECK_FREQUENCY = 10 // times per second the audio level is sampled

    private var mSensorManager: SensorManager? = null
    private var mProximity: Sensor? = null
    private var soundMeter: SoundMeter? = null
    private var permissionToRecordAccepted = false
    var level = Level.MIDDLE


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mProximity = mSensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        soundMeter = SoundMeter()

        // Get a Display object to access screen details
        val display = windowManager.defaultDisplay

        // Load the resolution into a Point object
        val resolution = Point()
        display.getRealSize(resolution)

        // And finally set the view for our game
        sideScrollView = SideScrollView(this, resolution.x, resolution.y)

        // Make our sideScrollView the view for the Activity
        setContentView(sideScrollView)


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) finish()
    }

    override fun onResume() {
        super.onResume()
        sideScrollView?.resume()

        window.decorView.systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_STABLE
                or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or SYSTEM_UI_FLAG_FULLSCREEN
                or SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        mSensorManager!!.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL)

        // Ask for permission to record audio, and start the sound meter
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        } else {
            soundMeter!!.start()
        }

        val handler = Handler()
        val delay = (1000/CHECK_FREQUENCY).toLong() //milliseconds

        handler.postDelayed(object : Runnable {
            override fun run() {
                val soundLevel = soundMeter?.amplitude
                if (soundLevel != null) {
                    if (soundLevel > SOUND_BARRIER) loudNoise()
                    else quietNoise()
                }
                if (!sideScrollView!!.running){
                   gameOver()
                } else {
                    handler.postDelayed(this, delay)
                }
            }
        }, delay)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager!!.unregisterListener(this)
        soundMeter!!.stop()
        sideScrollView?.pause()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] == event.sensor.maximumRange) handRemoved()
            else handCovered()
        }
    }

    private fun gameOver(){
        val intent = Intent(this, GameOverActivity::class.java)
        intent.putExtra("score", sideScrollView!!.score)
        startActivity(intent)
        finish()
    }


    private fun handCovered() {
        moveToBottom()
    }

    private fun handRemoved() {
        moveToMiddle()
    }

    fun loudNoise() {
        if (level == Level.MIDDLE) {
            moveToTop()
        }
    }

    fun quietNoise() {
        if (level == Level.TOP) {
            moveToMiddle()
        }
    }

    private fun moveToTop() {
        level = Level.TOP
        sideScrollView?.updateLevel(level)
    }

    private fun moveToMiddle() {
        level = Level.MIDDLE
        sideScrollView?.updateLevel(level)

    }

    private fun moveToBottom() {
        level = Level.BOTTOM
        sideScrollView?.updateLevel(level)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // we don't need to do anything here (but we promised to implement this as a SensorEventListener)
    }
}