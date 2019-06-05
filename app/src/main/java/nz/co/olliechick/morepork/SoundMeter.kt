package nz.co.olliechick.morepork

import android.media.MediaRecorder


/**
 * Adapted from https://stackoverflow.com/a/14181707/8355496
 */
class SoundMeter {

    private var mRecorder: MediaRecorder? = null

    val amplitude: Double
        get() = if (mRecorder != null) mRecorder!!.maxAmplitude.toDouble()
        else 0.0


    fun start() {
        if (mRecorder == null) {
            mRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile("/dev/null")
                prepare()
                start()
            }
        }
    }

    fun stop() {
        mRecorder?.apply {
            stop()
            release()
        }
        mRecorder = null
    }
}