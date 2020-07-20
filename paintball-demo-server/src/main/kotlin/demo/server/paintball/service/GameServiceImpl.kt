package demo.server.paintball.service

import demo.server.paintball.data.Game
import org.springframework.stereotype.Service

@Service
class GameServiceImpl : GameService {

    override fun getGame() = Game

    fun createTestGame() = Game.apply {
        name = "Test game"
        time = 120
        admin = "Sanyi"
        type = "TDM"
    }
}
