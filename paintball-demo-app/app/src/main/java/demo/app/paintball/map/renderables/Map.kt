package demo.app.paintball.map.renderables

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import demo.app.paintball.PaintballApplication
import demo.app.paintball.R

class Map : Renderable {

    companion object {
        var areaTopX = 0
        var areaTopY = 570

        const val minZoom = 4.8
        const val maxZoom = 1.5
        const val maxScaleFactor = 2

        var zoom = minZoom

        var playerPosX = 2897
        var playerPosY = 4050
    }

    private val image =
        BitmapFactory.decodeResource(
            PaintballApplication.context.resources,
            R.drawable.img_map_gyenes
        )

    private lateinit var bitmapDrawable: BitmapDrawable

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    private var translateX = 0
    private var translateY = 0

    override fun step() {
    }

    override fun setSize(x: Int, y: Int) {
        screenWidth = x
        screenHeight = y

        bitmapDrawable = BitmapDrawable(PaintballApplication.context.resources, image)
        bitmapDrawable.tileModeX = Shader.TileMode.MIRROR
        bitmapDrawable.tileModeY = Shader.TileMode.MIRROR
    }

    override fun render(canvas: Canvas) {
        calculateTranslate()

        val src = Rect(0, 0, image.width, image.height)
        val dst = Rect(
            translateX,
            translateY,
            translateX + (image.width / zoom).toInt(),
            translateY + (image.height / zoom).toInt()
        )
        canvas.drawBitmap(bitmapDrawable.bitmap, src, dst, null)
    }

    private fun calculateTranslate() {
        translateX = (screenWidth / 2 - playerPosX / zoom).toInt()
        translateY = (screenHeight / 2 - playerPosY / zoom).toInt()
    }

    // rescaling(2): https://en.wikipedia.org/wiki/Feature_scaling
    fun scale(scaleFactor: Float) {
        zoom = minZoom + (scaleFactor - 1) * (maxZoom - minZoom) / (maxScaleFactor - 1)
    }
}