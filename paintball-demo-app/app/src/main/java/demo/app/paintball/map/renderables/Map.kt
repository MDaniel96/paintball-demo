package demo.app.paintball.map.renderables

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import demo.app.paintball.PaintballApplication.Companion.context
import demo.app.paintball.R

class Map : Renderable() {

    companion object {
        const val minZoom = 4.8
        const val maxZoom = 1.5
        const val maxScaleFactor = 2

        var zoom = minZoom
    }

    private val image = BitmapFactory.decodeResource(context.resources, R.drawable.img_map_gyenes)

    private lateinit var bitmapDrawable: BitmapDrawable

    override fun setSize(x: Int, y: Int) {
        super.setSize(x, y)

        bitmapDrawable = BitmapDrawable(context.resources, image)
        bitmapDrawable.tileModeX = Shader.TileMode.MIRROR
        bitmapDrawable.tileModeY = Shader.TileMode.MIRROR
    }

    override fun render(canvas: Canvas) {
        val translateX = (screenWidth / 2 - Player.posX / zoom).toInt()
        val translateY = (screenHeight / 2 - Player.posY / zoom).toInt()

        val src = Rect(0, 0, image.width, image.height)
        val dst = Rect(
            translateX,
            translateY,
            translateX + (image.width / zoom).toInt(),
            translateY + (image.height / zoom).toInt()
        )
        canvas.drawBitmap(bitmapDrawable.bitmap, src, dst, null)
    }

    // rescaling(2): https://en.wikipedia.org/wiki/Feature_scaling
    fun scale(scaleFactor: Float) {
        zoom = minZoom + (scaleFactor - 1) * (maxZoom - minZoom) / (maxScaleFactor - 1)
    }
}