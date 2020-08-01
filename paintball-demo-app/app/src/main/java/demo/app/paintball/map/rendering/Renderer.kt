package demo.app.paintball.map.rendering

import android.graphics.Canvas
import demo.app.paintball.map.model.Map
import demo.app.paintball.map.model.Player
import demo.app.paintball.map.model.Renderable
import demo.app.paintball.map.model.TeamMate
import demo.app.paintball.util.clear

class Renderer(width: Int, height: Int) {

    private val entitiesToDraw = mutableListOf<Renderable>()

    private val map = Map()
    private val player = Player()

    private val enemy = TeamMate()

    init {
        map.setSize(width, height)
        player.setSize(width, height)

        enemy.setSize(width, height)

        entitiesToDraw.add(player)

        entitiesToDraw.add(enemy)
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