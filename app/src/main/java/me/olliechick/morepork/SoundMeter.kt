package me.olliechick.morepork

import android.R.attr.start
import android.media.MediaRecorder


/**
 * Adapted from https://stackoverflow.com/a/14181707/8355496
 */
class SoundMeter {

    private var mRecorder: MediaRecorder? = null

    val amplitude: Double
        get() = if (mRecorder != null)
            mRecorder!!.maxAmplitude.toDouble()
        else
            0.0


    fun start() {
        if (mRecorder == null) {
            mRecorder = MediaRecorder()
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mRecorder!!.setOutputFile("/dev/null")
            mRecorder!!.prepare()
            mRecorder!!.start()
        }
    }

    fun stop() {
        if (mRecorder != null) {
            mRecorder!!.stop()
            mRecorder!!.release()
            mRecorder = null
        }
    }
}