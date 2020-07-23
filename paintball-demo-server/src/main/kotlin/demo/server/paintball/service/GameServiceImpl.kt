package demo.server.paintball.service

import demo.server.paintball.data.Game
import demo.server.paintball.data.Player
import demo.server.paintball.mock.InitGameService
import org.springframework.stereotype.Service

@Service
class GameServiceImpl(val initGameService: InitGameService) : GameService {

    override fun getGame() = GameService.game

    override fun createGame(game: Game) {
        GameService.game = game
        initGameService.initTestPlayers()
    }

    override fun deleteGame() {
        GameService.game = null
    }

    override fun addRedPlayer(player: Player) {
        GameService.game?.redTeam?.add(player)
    }

    override fun addBluePlayer(player: Player) {
        GameService.game?.blueTeam?.add(player)
    }
}
