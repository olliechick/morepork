package nz.co.olliechick.morepork

import android.content.Context
import android.graphics.*
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.ArrayList

/**
 *  Handles animating background images.
 */
class SideScrollView internal constructor(internal var context: Context, var screenWidth: Int, var screenHeight: Int) :
    SurfaceView(context), Runnable {

    private var backgrounds: ArrayList<Background>
    private var obstacles: ArrayList<Obstacle>

    @Volatile
    private var running: Boolean = false
    private var gameThread: Thread? = null

    // For drawing
    private val paint: Paint = Paint()
    private var canvas: Canvas? = null
    private val ourHolder: SurfaceHolder = holder

    private var speed = 500f

    private var owlBitmap: Bitmap
    private var owlWidth: Int
    private var owlHeight: Int

    // Control the fps
    private var fps: Long = 60

    private var level: Level = Level.MIDDLE

    fun updateLevel(level: Level) {
        this.level = level
    }

    init {
        // Make a resource id out of the string of the file name
        val resID = context.resources.getIdentifier("morepork", "drawable", context.packageName)
        owlBitmap = BitmapFactory.decodeResource(context.resources, resID)

        // Initialize our array list
        backgrounds = ArrayList()
        obstacles = ArrayList()

        val top = 0
        val middle = (screenHeight * (1 / 3.toDouble())).toInt()
        val bottom = (screenHeight * (2 / 3.toDouble())).toInt()

        owlBitmap = Bitmap.createScaledBitmap(
            owlBitmap,
            (((screenHeight / 6.toDouble()).toInt() * owlBitmap.width) / (owlBitmap.height)),
            (screenHeight / 6.toDouble()).toInt(),
            true
        )

        owlWidth = owlBitmap.width
        owlHeight = owlBitmap.height


        backgrounds.add(Background(this.context, screenWidth, screenHeight, 0, 110, speed))

        val sY = 0
        val eY = 100

        val possibleObstacles = arrayOf(
            Obstacle(this.context, screenWidth, screenHeight, "drone", sY, eY, speed, 3.0F, middle),
            Obstacle(this.context, screenWidth, screenHeight, "tree", sY, eY, speed, 1.5F, middle),
            Obstacle(this.context, screenWidth, screenHeight, "branch", sY, eY, speed, 3.0F, top),
            Obstacle(this.context, screenWidth, screenHeight, "fern", sY, eY, speed, 3.0F, bottom)
        )

        for (i in 0..100) {
            val obstacle = possibleObstacles[(Math.floor(Math.random() * possibleObstacles.size)).toInt()].clone() as Obstacle
            obstacle.setOffset(i*1500)
            obstacles.add(obstacle)
        }
    }

    override fun run() {

        while (running) {
            val startFrameTime = System.currentTimeMillis()

            update()

            draw()
            drawOwl()
            drawObstacles()

            // Calculate the fps this frame
            val timeThisFrame = System.currentTimeMillis() - startFrameTime
            if (timeThisFrame >= 1) {
                fps = (1000 / timeThisFrame)
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


    private fun drawObstacle(obstacle: Obstacle) {

        // left   The X coordinate of the left side of the rectangle
        // top    The Y coordinate of the top of the rectangle
        // right  The X coordinate of the right side of the rectangle
        // bottom The Y coordinate of the bottom of the rectangle
        val leftSrc: Int
        val rightSrc: Int

        val leftDst: Int
        val rightDst: Int
        val topDst: Int
        val bottomDst: Int

        // Make a copy of the relevant background
        when {
            (obstacle.positionX + obstacle.width) > screenWidth -> {
                // clip right side of obstacle
                leftSrc = 0
                rightSrc = (obstacle.width - ((obstacle.positionX + obstacle.width) - screenWidth))

                leftDst = obstacle.positionX
                rightDst = screenWidth
                topDst = obstacle.positionY
                bottomDst = obstacle.positionY + obstacle.height
            }
            obstacle.positionX < 0 -> {
                // clip left side of obstacle
                leftSrc = (-obstacle.positionX)
                rightSrc = obstacle.width

                leftDst = 0
                rightDst = (obstacle.width + obstacle.positionX)
                topDst = obstacle.positionY
                bottomDst = obstacle.positionY + obstacle.height
            }
            else -> {
                leftSrc = 0
                rightSrc = obstacle.width

                leftDst = obstacle.positionX
                rightDst = obstacle.positionX + obstacle.width
                topDst = obstacle.positionY
                bottomDst = obstacle.positionY + obstacle.height
            }
        }
        val fromRect1 = Rect(leftSrc, 0, rightSrc, obstacle.height)
        val toRect1 = Rect(leftDst, topDst, rightDst, bottomDst)
        canvas!!.drawBitmap(obstacle.bitmap, fromRect1, toRect1, paint)

    }


    /**
     * Update all backgrounds (allows for multiple backgrounds)
     */
    private fun update() {
        // Update all the background positions
        for (bg in backgrounds) {
            bg.update(fps)
        }
        removeOldObstacles()
        for (obstacle in obstacles) {
            obstacle.update(fps)
        }
    }

    private fun removeOldObstacles() {
        val temp: ArrayList<Obstacle> = ArrayList()
        obstacles.forEach { obstacle ->
            if (obstacle.positionX > (-obstacle.width)) temp.add(obstacle) else obstacle.positionX = screenWidth
        }
        obstacles = temp
    }

    private fun drawObstacles() {
        if (holder.surface.isValid) {
            for (obstacle in obstacles) {
                drawObstacle(obstacle)
            }
            ourHolder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawOwl() {
        if (holder.surface.isValid) {
            val leftDst = (screenWidth * 0.5).toInt() - (owlWidth * 0.5).toInt()
            val rightDst = (screenWidth * 0.5).toInt() + (owlWidth * 0.5).toInt()
            val topDst: Int
            val bottomDst: Int
            if (level == Level.MIDDLE) {
                topDst = (screenHeight * 0.5).toInt() - (owlHeight * 0.5).toInt()
                bottomDst = (screenHeight * 0.5).toInt() + (owlHeight * 0.5).toInt()
            } else if (level == Level.BOTTOM) {
                topDst = screenHeight - owlHeight
                bottomDst = screenHeight
            } else {
                topDst = 0
                bottomDst = owlHeight
            }

            val fromRect1 = Rect(0, 0, owlWidth, owlHeight)
            val toRect1 = Rect(leftDst, topDst, rightDst, bottomDst)
            canvas!!.drawBitmap(owlBitmap, fromRect1, toRect1, paint)
        }
    }

    /**
     * Draw the background
     */
    private fun draw() {
        if (ourHolder.surface.isValid) {

            canvas = ourHolder.lockCanvas()
            drawBackground(0)

        }
    }
}