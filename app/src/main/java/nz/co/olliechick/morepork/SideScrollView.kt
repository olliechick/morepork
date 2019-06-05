package nz.co.olliechick.morepork

import android.content.Context
import android.graphics.*
import android.view.SurfaceView
import androidx.preference.PreferenceManager
import java.util.ArrayList

/**
 *  Handles animating background images.
 */
class SideScrollView internal constructor(internal var context: Context, var screenWidth: Int, var screenHeight: Int) :
    SurfaceView(context), Runnable {

    private var backgrounds: ArrayList<Background>
    private var obstacles: ArrayList<Obstacle>

    @Volatile
    var running: Boolean = true
    private var gameThread: Thread? = null

    // For drawing
    private val paint: Paint = Paint()
    private var canvas: Canvas? = null

    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    // Use string because there is no float-array in xml
    private var obstacleSpeed = 500f // default, will be replaced
    private var backgroundSpeed = 100f // default, will be replaced
    private val distanceBetweenObstacles = 1500

    private var owlBitmap: Bitmap
    private var owlWidth: Int
    private var owlHeight: Int

    // Control the fps
    private var fps: Long = 60

    private val obstacleTypes: Array<Array<Obstacle>>

    private var level = Level.MIDDLE
    var score = 0.0
    val SCORE_EASY_INCREMENT = 1.0
    val SCORE_MEDIUM_INCREMENT = 1.5
    val SCORE_HARD_INCREMENT = 2.0

    private var crashLevel = Level.MIDDLE

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

        val sY = 0
        val eY = 100

        // Get obstacle speed from prefs
        val obstacleSpeedString = sharedPreferences.getString("difficulty", "500")
        if (obstacleSpeedString != null) {
            val nullableSpeed = obstacleSpeedString.toFloatOrNull()
            if (nullableSpeed != null) obstacleSpeed = nullableSpeed
        }
        backgroundSpeed = obstacleSpeed / 5

        backgrounds.add(Background(this.context, screenWidth, screenHeight, 0, 110, backgroundSpeed))

        obstacleTypes = arrayOf(
            arrayOf(
                Obstacle(this.context, screenWidth, screenHeight, "drone", sY, eY, obstacleSpeed, 3.0F, middle),
                Obstacle(this.context, screenWidth, screenHeight, "drone", sY, eY, obstacleSpeed, 3.0F, top)
            ),
            arrayOf(Obstacle(this.context, screenWidth, screenHeight, "tree", sY, eY, obstacleSpeed, 1.5F, middle)),
            arrayOf(Obstacle(this.context, screenWidth, screenHeight, "branch", sY, eY, obstacleSpeed, 2F, top)),
            arrayOf(Obstacle(this.context, screenWidth, screenHeight, "fern", sY, eY, obstacleSpeed, 3.0F, bottom))
        )


        // Make some initial obstacles
        for (i in 0..10) {
            addObstacle()
        }
    }

    override fun run() {

        while (running) {
            val startFrameTime = System.currentTimeMillis()

            update()

            if (holder.surface.isValid) {
                canvas = holder.lockCanvas()
                draw()
                drawObstacles()
                drawOwl(level)
                drawScore()
                holder.unlockCanvasAndPost(canvas)
                if (checkForCollisions()) {
                    running = false
                    canvas = holder.lockCanvas()
                    draw()
                    drawObstacles()
                    drawOwl(crashLevel)
                    drawScore()
                    holder.unlockCanvasAndPost(canvas)
                }
            }

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

    private fun generateRandomObstacle(): Obstacle {
        val possibleObstacles = obstacleTypes[(Math.floor(Math.random() * obstacleTypes.size)).toInt()]
        val obstacleIndex = (Math.floor(Math.random() * possibleObstacles.size)).toInt()
        return possibleObstacles[obstacleIndex].clone() as Obstacle
    }

    private fun addObstacle() {
        val obstacle = generateRandomObstacle()
        obstacle.positionX = if (obstacles.isEmpty()) screenWidth * 3
        else {
            val lastObstacle = obstacles.last()
            lastObstacle.positionX + distanceBetweenObstacles
        }
        obstacles.add(obstacle)
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

        // Update obstacles
        val removedAnObstacle = removeOldObstacles()
        if (removedAnObstacle) addObstacle()
        for (obstacle in obstacles) {
            obstacle.update(fps)
        }
    }

    /**
     * If an obstacle has just moved off the screen, removes it and returns true.
     * Else, returns false
     */
    private fun removeOldObstacles(): Boolean {
        val tempObstacles: ArrayList<Obstacle> = ArrayList()
        var removedAnObstacle = false
        obstacles.forEach { obstacle ->
            if (obstacle.positionX > (-obstacle.width)) {
                tempObstacles.add(obstacle)
            } else {
                obstacle.positionX = screenWidth
                when (sharedPreferences.getString("difficulty", "500")) {
                    "300" -> score += SCORE_EASY_INCREMENT
                    "500" -> score += SCORE_MEDIUM_INCREMENT
                    "700" -> score += SCORE_HARD_INCREMENT
                }
                removedAnObstacle = true
            }
        }
        obstacles = tempObstacles
        return removedAnObstacle
    }

    private fun drawObstacles() {
        for (obstacle in obstacles) {
            drawObstacle(obstacle)
        }
    }

    private fun drawOwl(level: Level) {
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

    private fun checkForCollisions(): Boolean {
        val leftDst = (screenWidth * 0.5).toInt() - (owlWidth * 0.5).toInt()
        val rightDst = (screenWidth * 0.5).toInt() + (owlWidth * 0.5).toInt()
        val topDst: Int
        val bottomDst: Int
        if (level == Level.MIDDLE) {
            topDst = (screenHeight * 0.5).toInt() - (owlHeight * 0.5).toInt()
            bottomDst = (screenHeight * 0.5).toInt() + (owlHeight * 0.5).toInt()
            crashLevel = Level.MIDDLE
        } else if (level == Level.BOTTOM) {
            topDst = screenHeight - owlHeight
            bottomDst = screenHeight
            crashLevel = Level.BOTTOM
        } else {
            topDst = 0
            bottomDst = owlHeight
            crashLevel = Level.TOP
        }
        obstacles.forEach { obstacle ->
            if (obstacle.isOverlapping(leftDst, rightDst, topDst, bottomDst, owlBitmap)) {
                return true
            }
        }
        return false
    }


    private fun drawScore() {
        val path = Path()
        val text = Util.getStringFromDouble(score)
        val scoreLocationX = (screenWidth * 0.075).toFloat()
        val scoreLocationY = (screenHeight * 0.25).toFloat()

        paint.apply {
            color = Color.BLACK
            textSize = 200F //todo see issue 32
            style = Paint.Style.FILL
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
            getTextPath(text, 0, text.length, scoreLocationX, scoreLocationY, path)
        }

        val stkPaint = Paint()
        stkPaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = 20f
            color = Color.WHITE
        }

        canvas!!.apply {
            drawPath(path, stkPaint)
            drawPath(path, paint)
        }
    }

    /**
     * Draw the background
     */
    private fun draw() {
        drawBackground(0)
    }
}