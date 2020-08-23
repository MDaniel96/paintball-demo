package demo.app.paintball.map.renderables

import android.graphics.Canvas

abstract class Renderable {

    protected var screenWidth = 0
    protected var screenHeight = 0

    open fun setSize(x: Int, y: Int) {
        screenWidth = x
        screenHeight = y
    }

    open fun step() {
    }

    abstract fun render(canvas: Canvas)
}