package demo.app.paintball.map.rendering

import android.graphics.Canvas
import demo.app.paintball.map.renderables.*
import demo.app.paintball.map.renderables.Map
import demo.app.paintball.util.clear

class Renderer(private val width: Int, private val height: Int) {

    private val dotsToDraw = mutableListOf<Dot>()

    private val map = Map()
    private val player = Player()

    init {
        map.setSize(width, height)
        player.setSize(width, height)
    }

    fun step() {
        player.step()
        dotsToDraw.forEach(Renderable::step)
    }

    fun draw(canvas: Canvas) {
        canvas.clear()
        map.render(canvas)
        player.render(canvas)
        dotsToDraw.forEach { it.render(canvas) }
    }

    fun setPlayerPosition(posX: Int, posY: Int) {
        Map.playerPosX = posX
        Map.playerPosY = posY
    }

    fun setPlayerOrientation(degree: Float) {
        player.setOrientation(degree)
    }

    fun setDotPosition(playerName: String, posX: Int, posY: Int) {
        dotsToDraw.filter { it.name == playerName }
            .forEach {
                it.posX = posX
                it.posY = posY
            }
    }

    fun zoom(scaleFactor: Float) {
        map.scale(scaleFactor)
    }

    fun addRedPlayer(playerName: String) {
        val redPlayer = RedPlayer(playerName)
        redPlayer.setSize(width, height)
        dotsToDraw.add(redPlayer)
    }

    fun addBluePlayer(playerName: String) {
        val bluePlayer = BluePlayer(playerName)
        bluePlayer.setSize(width, height)
        dotsToDraw.add(bluePlayer)
    }
}