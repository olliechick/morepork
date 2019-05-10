package nz.co.olliechick.morepork

import android.Manifest
import android.content.Context
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


class GameActivity : AppCompatActivity(), SensorEventListener {

    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val SOUND_BARRIER = 2000 // how loud it has to be to move the avatar up
    private val CHECK_FREQUENCY = 10 // times per second the audio level is sampled

    private var mSensorManager: SensorManager? = null
    private var mProximity: Sensor? = null
    private var soundMeter: SoundMeter? = null
    private var permissionToRecordAccepted = false
    private var level = Level.MIDDLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mProximity = mSensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        soundMeter = SoundMeter()
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
//              Toast.makeText(applicationContext, "sound level = ${soundMeter?.amplitude}", Toast.LENGTH_SHORT).show()
                if (soundLevel != null) {
                    if (soundLevel > SOUND_BARRIER) loudNoise()
                    else quietNoise()
                }
                handler.postDelayed(this, delay)
            }
        }, delay)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager!!.unregisterListener(this)
        soundMeter!!.stop()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] == event.sensor.maximumRange) handRemoved()
            else handCovered()
        }
    }

    fun handCovered() {
        moveToBottom()
    }

    fun handRemoved() {
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

    fun moveToTop() {
        level = Level.TOP
        Toast.makeText(applicationContext, "moving to top", Toast.LENGTH_SHORT).show()
    }

    fun moveToMiddle() {
        level = Level.MIDDLE
        Toast.makeText(applicationContext, "moving to middle", Toast.LENGTH_SHORT).show()
    }

    fun moveToBottom() {
        level = Level.BOTTOM
        Toast.makeText(applicationContext, "moving to bottom", Toast.LENGTH_SHORT).show()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // we don't need to do anything here (but we promised to implement this as a SensorEventListener)
    }
}
