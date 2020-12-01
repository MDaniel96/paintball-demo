package demo.server.paintball.service

import demo.server.paintball.data.Game
import demo.server.paintball.data.Player
import demo.server.paintball.service.test.TestService
import demo.server.paintball.service.test.TestServiceImpl
import org.springframework.stereotype.Service

@Service
class GameServiceImpl : GameService {

    override var game: Game? = null

    override fun deleteGame() {
        game = null
    }

    override fun addRedPlayer(player: Player) {
        game?.blueTeam?.remove(player)

        player.team = Player.Team.RED
        game?.redTeam?.add(player)
    }

    override fun addBluePlayer(player: Player) {
        game?.redTeam?.remove(player)

        player.team = Player.Team.BLUE
        game?.blueTeam?.add(player)
    }
}
