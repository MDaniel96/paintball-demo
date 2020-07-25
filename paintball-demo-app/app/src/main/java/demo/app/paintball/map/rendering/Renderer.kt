package demo.app.paintball.map.rendering

import android.content.Context
import android.graphics.Canvas
import demo.app.paintball.map.model.Background
import demo.app.paintball.map.model.Player
import demo.app.paintball.map.model.Renderable
import demo.app.paintball.util.clear

class Renderer(
    context: Context,
    width: Int,
    height: Int
) {
    private val entitiesToDraw = mutableListOf<Renderable>()

    private val background = Background(context)
    private val player = Player(context)

    init {
        background.setSize(width, height)
        player.setSize(width, height)

        entitiesToDraw.add(player)
    }

    fun step() {
        entitiesToDraw.forEach(Renderable::step)
    }

    fun draw(canvas: Canvas) {
        canvas.clear()
        background.render(canvas)
        entitiesToDraw.forEach { it.render(canvas) }
    }

    fun setPlayerPosition(posX: Int, posY: Int) {
        Background.playerPosX = posX
        Background.playerPosY = posY
    }

    fun zoom(scaleFactor: Float) {
        background.scale(scaleFactor)
    }
}