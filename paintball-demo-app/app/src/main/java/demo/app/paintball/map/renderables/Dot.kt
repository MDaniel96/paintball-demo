package demo.app.paintball.map.renderables

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect

abstract class Dot(val name: String) : Renderable {

    companion object {
        const val dotSize = 3
    }

    protected abstract val image: Bitmap

    abstract var posX: Int
    abstract var posY: Int

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    private var translateX = 0
    private var translateY = 0

    override fun step() {
    }

    override fun setSize(x: Int, y: Int) {
        screenWidth = x
        screenHeight = y
    }

    override fun render(canvas: Canvas) {
        calculateTranslate()

        val src = Rect(0, 0, image.width, image.height)
        val dst = Rect(
            translateX,
            translateY,
            translateX + image.width / dotSize,
            translateY + image.height / dotSize
        )
        canvas.drawBitmap(image, src, dst, null)
    }

    private fun calculateTranslate() {
        val distanceFromPlayerX = (Map.playerPosX - posX) / Map.zoom
        val distanceFromPlayerY = (Map.playerPosY - posY) / Map.zoom

        translateX = (screenWidth / 2 - distanceFromPlayerX).toInt()
        translateY = (screenHeight / 2 - distanceFromPlayerY).toInt()
    }
}