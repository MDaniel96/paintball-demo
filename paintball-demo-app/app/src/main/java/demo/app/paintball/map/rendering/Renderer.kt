package demo.app.paintball.map.rendering

import android.content.Context
import android.graphics.Canvas
import demo.app.paintball.map.model.Map
import demo.app.paintball.map.model.Player
import demo.app.paintball.map.model.Renderable
import demo.app.paintball.util.clear

class Renderer(
    context: Context,
    width: Int,
    height: Int
) {
    private val entitiesToDraw = mutableListOf<Renderable>()

    private val map = Map(context)
    private val player = Player(context)

    init {
        map.setSize(width, height)
        player.setSize(width, height)

        entitiesToDraw.add(player)
    }

    fun step() {
        entitiesToDraw.forEach(Renderable::step)
    }

    fun draw(canvas: Canvas) {
        canvas.clear()
        map.render(canvas)
        entitiesToDraw.forEach { it.render(canvas) }
    }

    fun setPlayerPosition(posX: Int, posY: Int) {
        Map.playerPosX = posX
        Map.playerPosY = posY
    }

    fun setPlayerOrientation(degree: Float) {
        player.setOrientation(degree)
    }

    fun zoom(scaleFactor: Float) {
        map.scale(scaleFactor)
    }
}