package me.olliechick.morepork

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.hardware.Sensor
import android.hardware.SensorEventListener


class GameActivity : AppCompatActivity(), SensorEventListener {

    private var mSensorManager: SensorManager? = null
    private var mProximity: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)


        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mProximity = mSensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    }

    override fun onResume() {
        super.onResume()
        mSensorManager!!.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        mSensorManager!!.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] == event.sensor.maximumRange) {
                //far
                Toast.makeText(applicationContext, "far", Toast.LENGTH_SHORT).show()
            } else {
                //near
                Toast.makeText(applicationContext, "near", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // we don't need to do anything here (but we promised to implement this as a SensorEventListener)
    }
}
