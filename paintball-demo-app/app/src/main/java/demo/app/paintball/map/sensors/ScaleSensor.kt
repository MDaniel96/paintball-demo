package demo.app.paintball.map.sensors

import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import android.view.View.OnTouchListener
import demo.app.paintball.PaintballApplication


class ScaleSensor(val scaleListener: ScaleListener) : OnTouchListener,
    OnScaleGestureListener {

    companion object {
        const val zoomLimit = 0.2F
    }

    private val gestureScale: ScaleGestureDetector =
        ScaleGestureDetector(PaintballApplication.context, this)

    private var scaleFactor = 1F
    private var lastScale = 1F

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        gestureScale.onTouchEvent(event)
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        scaleFactor *= detector.scaleFactor
        // Prevent our view from becoming too small, too big
        scaleFactor = if (scaleFactor < 1) 1F else scaleFactor
        scaleFactor = if (scaleFactor > 2) 2F else scaleFactor
        // Change precision to help with jitter when user just rests their fingers
        scaleFactor = (scaleFactor * 100).toInt().toFloat() / 100
        scaleListener.onScaleChanged(scaleFactor)
        val scaleDiff = scaleFactor - lastScale
        if (scaleDiff > zoomLimit) {
            scaleListener.onZoomIn()
            lastScale = scaleFactor
        } else if (scaleDiff < -zoomLimit) {
            scaleListener.onZoomOut()
            lastScale = scaleFactor
        }
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
    }

    interface ScaleListener {
        fun onScaleChanged(scaleFactor: Float)
        fun onZoomIn()
        fun onZoomOut()
    }
}