package nz.co.olliechick.morepork

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.preference.PreferenceManager
import java.util.ArrayList

/**
 *  Handles animating background images.
 */
class SideScrollView internal constructor(internal var context: Context, var screenWidth: Int, internal var screenHeight: Int) : SurfaceView(context), Runnable {

    private var backgrounds: ArrayList<Background>

    @Volatile
    private var running: Boolean = false
    private var gameThread: Thread? = null

    // For drawing
    private val paint: Paint
    private var canvas: Canvas? = null
    private val ourHolder: SurfaceHolder
    private val prefs: SharedPreferences

    // Control the fps
    private var fps: Long = 60

    init {

        // Initialize our drawing objects
        ourHolder = holder
        paint = Paint()

        // Initialize our array list
        backgrounds = ArrayList()

        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val theme = prefs.getString("theme", "drawable")!!

        backgrounds.add(Background(this.context, screenWidth, screenHeight, theme, 0, 110, 200f))
    }

    override fun run() {

        while (running) {
            val startFrameTime = System.currentTimeMillis()

            update()

            draw()

            // Calculate the fps this frame
            val timeThisFrame = System.currentTimeMillis() - startFrameTime
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame
            }
        }
    }


    // Clean up our thread if the game is stopped
    fun pause() {
        running = false
        try {
            gameThread!!.join()
        } catch (e: InterruptedException) {
            // Error
        }

    }

    // Make a new thread and start it
    // Execution moves to our run method
    fun resume() {
        running = true
        gameThread = Thread(this)
        gameThread!!.start()
    }

    private fun drawBackground(position: Int) {

        // Make a copy of the relevant background
        val bg = backgrounds[position]

        // define what portion of images to capture and
        // what coordinates of screen to draw them at

        // For the regular bitmap
        val fromRect1 = Rect(0, 0, bg.width - bg.xClip, bg.height)
        val toRect1 = Rect(bg.xClip, bg.startY, bg.width, bg.endY)

        // For the reversed background
        val fromRect2 = Rect(bg.width - bg.xClip, 0, bg.width, bg.height)
        val toRect2 = Rect(0, bg.startY, bg.xClip, bg.endY)

        canvas!!.drawBitmap(bg.bitmap, fromRect1, toRect1, paint)
        canvas!!.drawBitmap(bg.bitmap, fromRect2, toRect2, paint)

    }

    /**
     * Update all backgrounds (allows for multiple backgrounds)
     */
    private fun update() {
        // Update all the background positions
        for (bg in backgrounds) {
            bg.update(fps)
        }
    }

    /**
     * Draw the background
     */
    private fun draw() {
        if (ourHolder.surface.isValid) {

            canvas = ourHolder.lockCanvas()
            canvas!!.drawColor(Color.argb(255, 0, 3, 70))
            drawBackground(0)
            ourHolder.unlockCanvasAndPost(canvas)
        }
    }
}