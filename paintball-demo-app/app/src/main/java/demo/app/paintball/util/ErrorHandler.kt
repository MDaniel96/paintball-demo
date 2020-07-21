package demo.app.paintball.util

import demo.app.paintball.data.game.GameManager

object ErrorHandler : GameManager.ErrorListener {

    override fun getGameFailure(t: Throwable) {
        toast("$t")
    }

    override fun createGameFailure(t: Throwable) {
        toast("$t")
    }

    override fun deleteGameFailure(t: Throwable) {
        toast("$t")
    }
}