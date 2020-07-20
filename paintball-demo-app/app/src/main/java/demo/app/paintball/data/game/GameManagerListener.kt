package demo.app.paintball.data.game

import demo.app.paintball.data.model.Game
import retrofit2.Response

interface GameManagerListener {

    fun getGameSuccess(response: Response<Game>)

    fun getGameFailure(t: Throwable)
}