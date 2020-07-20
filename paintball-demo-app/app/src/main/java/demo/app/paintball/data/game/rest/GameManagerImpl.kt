package demo.app.paintball.data.game.rest

import demo.app.paintball.data.game.GameManager
import demo.app.paintball.data.game.GameManagerListener
import demo.app.paintball.data.model.Game
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GameManagerImpl(override var listener: GameManagerListener) : GameManager {

    private val gameService: GameService = GameService.create()

    override fun getGame() {
        gameService.getGame().enqueue(object : Callback<Game> {
            override fun onResponse(call: Call<Game>, response: Response<Game>) {
                listener.getGameSuccess(response)
            }

            override fun onFailure(call: Call<Game>, t: Throwable) {
                listener.getGameFailure(t)
            }
        })
    }
}