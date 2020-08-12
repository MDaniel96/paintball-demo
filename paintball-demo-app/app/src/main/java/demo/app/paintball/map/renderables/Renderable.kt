package demo.app.paintball.map.renderables

import android.graphics.Canvas

interface Renderable {

    fun step()

    fun setSize(x: Int, y: Int)

    fun render(canvas: Canvas)
}