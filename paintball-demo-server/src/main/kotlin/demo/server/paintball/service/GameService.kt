package demo.server.paintball.service

import demo.server.paintball.data.Game

interface GameService {

    companion object {
        var game: Game? = null
    }

    fun getGame(): Game?

    fun createGame(game: Game)

    fun deleteGame()
}