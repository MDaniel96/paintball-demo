package demo.server.paintball.service

import demo.server.paintball.data.Game
import org.springframework.stereotype.Service

@Service
class GameServiceImpl : GameService {

    override fun getGame() = GameService.game

    override fun createGame(game: Game) {
        GameService.game = game
    }

    override fun deleteGame() {
        GameService.game = null
    }

    fun createTestGame() {
        GameService.game = Game().apply {
            name = "Test game"
            time = 120
            admin = "Sanyi"
            type = "TDM"
        }
    }
}
