package demo.app.paintball.map.sensors

import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import android.view.View.OnTouchListener
import demo.app.paintball.PaintballApplication


class ScaleSensor(val scaleListener: ScaleListener) : OnTouchListener,
    OnScaleGestureListener {

    private val gestureScale: ScaleGestureDetector =
        ScaleGestureDetector(PaintballApplication.context, this)

    private lateinit var view: View

    private var scaleFactor = 1F

    private var inScale = false

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        this.view = view
        gestureScale.onTouchEvent(event)
        return true
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        scaleFactor *= detector.scaleFactor
        // Prevent our view from becoming too small, too big
        scaleFactor = if (scaleFactor < 1) 1F else scaleFactor
        scaleFactor = if (scaleFactor > 2) 2F else scaleFactor
        // Change precision to help with jitter when user just rests their fingers //
        scaleFactor = (scaleFactor * 100).toInt().toFloat() / 100
        scaleListener.onScaleChanged(scaleFactor)
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        inScale = true
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        inScale = false
    }

    interface ScaleListener {
        fun onScaleChanged(scaleFactor: Float)
    }
}