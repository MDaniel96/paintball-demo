package demo.server.paintball.mock

import demo.server.paintball.config.AppConfig
import demo.server.paintball.data.Player
import demo.server.paintball.service.GameService
import org.springframework.stereotype.Service

@Service
class InitGameService(private val appConfig: AppConfig) {

    fun initTestPlayers() {
        if (appConfig.environment == "test") {
            val redPlayer1 = Player().apply { name = "Dani" }
            val redPlayer2 = Player().apply { name = "Sanyi" }
            val redPlayer3 = Player().apply { name = "Géza" }
            val bluePlayer1 = Player().apply { name = "Máté" }
            val bluePlayer2 = Player().apply { name = "Ákos" }
            val bluePlayer3 = Player().apply { name = "Lali" }

            GameService.game?.redTeam?.addAll(
                    mutableListOf(redPlayer1, redPlayer2, redPlayer3)
            )
            GameService.game?.blueTeam?.addAll(
                    mutableListOf(bluePlayer1, bluePlayer2, bluePlayer3)
            )
        }
    }
}