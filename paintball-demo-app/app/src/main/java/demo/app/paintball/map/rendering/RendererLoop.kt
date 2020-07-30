package demo.app.paintball.map.rendering

import android.content.Context
import android.graphics.Canvas

class RenderLoop(
    context: Context,
    private val view: MapView,
    width: Int,
    height: Int
) : Thread() {

    companion object {
        private const val FPS: Long = 30
        private const val TIME_BETWEEN_FRAMES = 1000 / FPS
    }

    private val renderer = Renderer(context, width, height)

    var running = false

    private fun getTime() = System.currentTimeMillis()

    override fun run() {
        while (running) {
            val renderStart = getTime()
            draw()
            val renderEnd = getTime()

            val sleepTime = TIME_BETWEEN_FRAMES - (renderEnd - renderStart)
            if (sleepTime > 0) {
                sleep(sleepTime)
            } else {
                sleep(5)
            }
        }
    }

    private fun draw() {
        renderer.step()

        var canvas: Canvas? = null

        try {
            canvas = view.holder.lockCanvas()
            synchronized(view.holder) {
                renderer.draw(canvas)
            }
        } finally {
            if (canvas != null) {
                view.holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    fun setPlayerPosition(posX: Int, posY: Int) {
        renderer.setPlayerPosition(posX, posY)
    }

    fun setPlayerOrientation(degree: Float) {
        renderer.setPlayerOrientation(degree)
    }

    fun zoom(scaleFactor: Float) {
        renderer.zoom(scaleFactor)
    }
}