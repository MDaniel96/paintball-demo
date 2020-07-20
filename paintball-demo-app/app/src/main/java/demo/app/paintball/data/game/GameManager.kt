package demo.app.paintball.data.game

interface GameManager {

    var listener: GameManagerListener

    /**
     * Gets current game or no game
     */
    fun getGame()
}