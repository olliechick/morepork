package nz.co.olliechick.morepork

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect


/**
 * Holds scaled bitmap of obstacle along with current position on screen for
 * rendering.
 */
class Obstacle internal constructor(
    context: Context,
    var screenWidth: Int,
    screenHeight: Int,
    bitmapName: String,
    sY: Int,
    eY: Int,
    var speed: Float,
    var scalingFactor: Float,
    var positionY: Int
) : Cloneable {

    internal var bitmap: Bitmap

    internal var width: Int = 0
    internal var height: Int = 0

    internal var positionX: Int = screenWidth


    internal var xClip: Int = 0
    internal var startY: Int = 0
    internal var endY: Int = 0

    init {

        // Make a resource id out of the string of the file name
        val resID = context.resources.getIdentifier(bitmapName, "drawable", context.packageName)

        // Load the bitmap using the id
        bitmap = BitmapFactory.decodeResource(context.resources, resID)

        //Position the background vertically
        startY = sY * (screenHeight / 100)
        endY = eY * (screenHeight / 100)

        // Create the bitmap
        bitmap = Bitmap.createScaledBitmap(
            bitmap,
            (((screenHeight / scalingFactor).toInt() * bitmap.width) / (bitmap.height)),
            screenHeight / scalingFactor.toInt(),
            true
        )

        // Save the width and height for later use
        width = bitmap.width
        height = bitmap.height
    }

    fun update(fps: Long) {
        positionX -= (speed / fps).toInt()
    }

    /**
     * First we check if the rectangle bounds of the owl bitmap and the obstacle bitmap overlap.
     * If they do then we call isCollisionDetected to check if any non-transparent parts overlap,
     * if they do we return true for a collision, otherwise we return false.
     */
    fun isOverlapping(left: Int, right: Int, top: Int, bottom: Int, bitmapOther: Bitmap): Boolean {
        if (((positionX + width) > left && positionX < right) &&
            (positionY < bottom && (positionY + height) > top)
        ) {
            if (isCollisionDetected(bitmapOther, left, top, bitmap, positionX, positionY)) {
                return true
            }
        }
        return false
    }


    /**
     * Check pixel-perfectly if two views are colliding
     */
    private fun isCollisionDetected(
        bitmap1: Bitmap, x1: Int, y1: Int,
        bitmap2: Bitmap, x2: Int, y2: Int
    ): Boolean {

        val bounds1 = Rect(x1, y1, x1 + bitmap1.width, y1 + bitmap1.height)
        val bounds2 = Rect(x2, y2, x2 + bitmap2.width, y2 + bitmap2.height)

        if (Rect.intersects(bounds1, bounds2)) {
            val collisionBounds = getCollisionBounds(bounds1, bounds2)
            for (i in collisionBounds.left until collisionBounds.right step 10) {
                for (j in collisionBounds.top until collisionBounds.bottom step 10) {
                    val bitmap1Pixel = bitmap1.getPixel(i - x1, j - y1)
                    val bitmap2Pixel = bitmap2.getPixel(i - x2, j - y2)
                    if ((bitmap1Pixel != Color.TRANSPARENT) && (bitmap2Pixel != Color.TRANSPARENT)) {
                        return true
                    }
                }
            }
        }
        return false
    }


    /**
     * Returns the rectangle that defines the overlap of the two
     * given rectangles.
     */
    private fun getCollisionBounds(rect1: Rect, rect2: Rect): Rect {
        return Rect(
            Math.max(rect1.left, rect2.left),
            Math.max(rect1.top, rect2.top),
            Math.min(rect1.right, rect2.right),
            Math.min(rect1.bottom, rect2.bottom)
        )
    }


    public override fun clone(): Any {
        return super.clone()
    }
}
