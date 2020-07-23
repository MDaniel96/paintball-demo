package demo.app.paintball.util

import demo.app.paintball.data.rest.GameManager

object ErrorHandler : GameManager.ErrorListener {

    override fun handleError(t: Throwable) {
        toast("$t")
    }
}