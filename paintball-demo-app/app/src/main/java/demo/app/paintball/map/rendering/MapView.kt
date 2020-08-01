package demo.app.paintball.map.rendering

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class MapView : SurfaceView {

    private var renderLoop: RenderLoop? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                // empty
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                var retry = true
                renderLoop?.running = false
                while (retry) {
                    try {
                        renderLoop?.join()
                        retry = false
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                val loop = RenderLoop(this@MapView, width, height)
                loop.running = true
                loop.start()

                renderLoop = loop
            }
        })
    }

    fun setPlayerPosition(posX: Int, posY: Int) {
        renderLoop?.setPlayerPosition(posX, posY)
    }

    fun setPlayerOrientation(degree: Float) {
        renderLoop?.setPlayerOrientation(degree)
    }

    fun zoom(scaleFactor: Float) {
        renderLoop?.zoom(scaleFactor)
    }
}