package demo.server.paintball.service

import demo.server.paintball.data.Game
import org.springframework.stereotype.Service

@Service
class GameServiceImpl : GameService {

    private var game: Game? = null

    override fun getGame() = game

    fun createTestGame() = Game(
            name = "test game",
            time = 12,
            admin = "Sanyi",
            type = "TDM"
    )
}