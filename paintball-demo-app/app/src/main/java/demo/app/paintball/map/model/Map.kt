package demo.app.paintball.map.model

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import demo.app.paintball.R

class Map(private val context: Context) : Renderable {

    companion object {
        var areaTopX = 0
        var areaTopY = 570

        const val minZoom = 4.8
        const val maxZoom = 1.5
        const val maxScaleFactor = 2

        var playerPosX = 2897
        var playerPosY = 4050
    }

    private val image = BitmapFactory.decodeResource(context.resources, R.drawable.car_map)

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private lateinit var bitmapDrawable: BitmapDrawable

    private var imageX = 0
    private var imageY = 0

    var topX = areaTopX
    var topY = areaTopY
    var zoom = minZoom

    var translateX = 0
    var translateY = 0

    override fun step() {
    }

    override fun setSize(x: Int, y: Int) {
        screenWidth = x
        screenHeight = y

        imageX = image.width
        imageY = image.height

        bitmapDrawable = BitmapDrawable(context.resources, image)
        bitmapDrawable.tileModeX = Shader.TileMode.MIRROR
        bitmapDrawable.tileModeY = Shader.TileMode.MIRROR
    }

    override fun render(canvas: Canvas) {
        calculateTranslate()

        val src = Rect(0, 0, imageX, imageY)
        val dest = Rect(
            translateX,
            translateY,
            translateX + (imageX / zoom).toInt(),
            translateY + (imageY / zoom).toInt()
        )
        canvas.drawBitmap(bitmapDrawable.bitmap, src, dest, null)
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