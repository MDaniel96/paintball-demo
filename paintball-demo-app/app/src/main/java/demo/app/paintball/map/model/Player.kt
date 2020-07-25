package demo.app.paintball.map.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import demo.app.paintball.R

class Player(context: Context) : Renderable {

    private var screenWidth = 0
    private var screenHeight = 0

    private val image: Bitmap =
        BitmapFactory.decodeResource(context.resources, R.drawable.player_arrow)

    companion object {
        var imageX = 0
        var imageY = 0
    }

    override fun step() {
    }

    override fun setSize(x: Int, y: Int) {
        screenWidth = x
        screenHeight = y

        imageX = image.width
        imageY = image.height
    }

    override fun render(canvas: Canvas) {
        val src = Rect(0, 0, imageX, imageY)
        val dst = Rect(
            screenWidth / 2,
            screenHeight / 2,
            screenWidth / 2 + imageX / 3,
            screenHeight / 2 + imageY / 3
        )
        canvas.drawBitmap(image, src, dst, null)
    }
}