package demo.app.paintball.data.game.rest

import demo.app.paintball.data.game.GameManager
import demo.app.paintball.data.model.Game
import demo.app.paintball.util.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GameManagerImpl(
    override var listener: GameManager.SuccessListener,
    override var errorListener: GameManager.ErrorListener
) : GameManager {

    private val gameService: GameService = GameService.create()

    override fun getGame() {
        gameService.getGame().enqueue(object : Callback<Game> {
            override fun onResponse(call: Call<Game>, response: Response<Game>) {
                listener.getGameSuccess(response)
            }

            override fun onFailure(call: Call<Game>, t: Throwable) {
                errorListener.getGameFailure(t)
            }
        })
    }

    override fun createGame(game: Game) {
        gameService.createGame(game).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                listener.createGameSuccess()
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                errorListener.createGameFailure(t)
            }
        })
    }

    override fun deleteGame() {
        gameService.deleteGame().enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                toast("Game deleted")
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                errorListener.deleteGameFailure(t)
            }
        })
    }
}