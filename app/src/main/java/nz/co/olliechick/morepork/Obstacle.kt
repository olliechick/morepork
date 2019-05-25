package nz.co.olliechick.morepork

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory


/**
 * Holds scaled bitmap of obstacle along with current position on screen for
 * rendering.
 */
class Obstacle internal constructor(context: Context,
                                    screenWidth: Int,
                                    screenHeight: Int,
                                    bitmapName: String,
                                    sY: Int, eY: Int,
                                    var speed: Float,
                                    var scalingFactor : Float,
                                    var positionY : Int) {

    internal var bitmap: Bitmap

    internal var width: Int = 0
    internal var height: Int = 0

    internal var positionX : Int = screenWidth


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
        bitmap = Bitmap.createScaledBitmap(bitmap,
            (((screenHeight/scalingFactor).toInt()*bitmap.width)/(bitmap.height)), screenHeight/scalingFactor.toInt(), true)

        // Save the width and height for later use
        width = bitmap.width
        height = bitmap.height
    }


    fun update(fps: Long) {
        positionX -= (speed / fps).toInt()
    }
}
