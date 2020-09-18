package demo.app.paintball.map.renderables

import android.graphics.*
import demo.app.paintball.PaintballApplication.Companion.context
import demo.app.paintball.R


class Player : Renderable() {

    companion object {
        const val size = 3
        const val phoneOrientation = 90.0F  // east
        const val mapOrientation = 270.0F  // ~ west
    }

    private var screenCenterX = 0F
    private var screenCenterY = 0F

    private val matrix = Matrix()
    private val image: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_player_arrow)

    override fun setSize(x: Int, y: Int) {
        super.setSize(x, y)

        screenCenterX = (x / 2).toFloat()
        screenCenterY = (y / 2).toFloat()

        setOrientation(0F)
    }

    override fun render(canvas: Canvas) {
        canvas.drawBitmap(image, matrix, null)
    }

    fun setOrientation(degree: Float) {
        val src = RectF(0F, 0F, image.width.toFloat(), image.height.toFloat())
        val dst = RectF(
            screenCenterX,
            screenCenterY,
            screenCenterX + image.width.toFloat() / size,
            screenCenterY + image.height.toFloat() / size
        )
        matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER)

        val phoneDegree = (degree + phoneOrientation) % 360.0F
        val mapDegree = (phoneDegree - mapOrientation) % 360.0F
        matrix.postRotate(
            mapDegree,
            screenCenterX + (image.width / 2) / size,
            screenCenterY + (image.height / 2) / size
        )
    }
}