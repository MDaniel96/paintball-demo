package demo.app.paintball.map.renderables

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.SystemClock

abstract class Dot(val name: String) : Renderable() {

    companion object {
        const val size = 3
        const val MAX_TIME_BETWEEN_POSITION_UPDATES = 1_500
    }

    protected abstract val image: Bitmap

    var posX: Int = 0
    var posY: Int = 0
        set(value) {
            field = value
            lastUpdate = SystemClock.uptimeMillis()
        }

    private var lastUpdate: Long = SystemClock.uptimeMillis()

    override fun render(canvas: Canvas) {
        if (isVisible()) {
            val distanceFromPlayerX = (Map.playerPosX - posX) / Map.zoom
            val distanceFromPlayerY = (Map.playerPosY - posY) / Map.zoom
            val translateX = (screenWidth / 2 - distanceFromPlayerX).toInt()
            val translateY = (screenHeight / 2 - distanceFromPlayerY).toInt()

            val src = Rect(0, 0, image.width, image.height)
            val dst = Rect(
                translateX,
                translateY,
                translateX + image.width / size,
                translateY + image.height / size
            )
            canvas.drawBitmap(image, src, dst, null)
        }
    }

    private fun isVisible() =
        (SystemClock.uptimeMillis() - lastUpdate) < MAX_TIME_BETWEEN_POSITION_UPDATES
}