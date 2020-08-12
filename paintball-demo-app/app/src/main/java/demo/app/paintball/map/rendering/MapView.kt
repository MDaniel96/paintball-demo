package demo.app.paintball.map.rendering

import android.view.View

interface MapView {

    fun setPlayerPosition(posX: Int, posY: Int)

    fun setDotPosition(playerName: String, posX: Int, posY: Int)

    fun setPlayerOrientation(degree: Float)

    fun addRedPlayer(playerName: String)

    fun addBluePlayer(playerName: String)

    fun zoom(scaleFactor: Float)

    fun setOnTouchListener(listener: View.OnTouchListener)
}