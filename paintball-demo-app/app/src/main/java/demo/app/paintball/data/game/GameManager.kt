package demo.app.paintball.data.game

import demo.app.paintball.data.model.Game
import retrofit2.Response

interface GameManager {

    var listener: SuccessListener

    var errorListener: ErrorListener

    /**
     * Gets current game or no game
     */
    fun getGame()

    /**
     * Creates game
     */
    fun createGame(game: Game)

    /**
     * Deletes game
     */
    fun deleteGame()

    /**
     * Listeners
     */
    interface SuccessListener {
        fun getGameSuccess(response: Response<Game>)
        fun createGameSuccess()
    }

    interface ErrorListener {
        fun getGameFailure(t: Throwable)
        fun createGameFailure(t: Throwable)
        fun deleteGameFailure(t: Throwable)
    }
}