package nz.co.olliechick.morepork

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory



/**
 * Background image
 */
class Background internal constructor(context: Context, screenWidth: Int, screenHeight: Int, bitmapName: String, sY: Int, eY: Int, var speed: Float) {

    internal var bitmap: Bitmap

    internal var width: Int = 0
    internal var height: Int = 0

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
        bitmap = Bitmap.createScaledBitmap(bitmap, ((screenHeight*bitmap.width)/(bitmap.height)), screenHeight, true)

        // Save the width and height for later use
        width = bitmap.width
        height = bitmap.height
    }


    fun update(fps: Long) {

        // Move the clipping position and reverse if necessary
        xClip -= (speed / fps).toInt()
        if (xClip >= width) {
            xClip = 0
        } else if (xClip <= 0) {
            xClip = width
        }
    }
}
