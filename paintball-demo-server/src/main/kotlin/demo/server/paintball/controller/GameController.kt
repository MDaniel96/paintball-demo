package demo.server.paintball.controller

import demo.server.paintball.data.Game
import demo.server.paintball.data.Player
import demo.server.paintball.service.GameService
import demo.server.paintball.service.test.TestService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
@RestController
@RequestMapping(GameController.BASE_URL)
class GameController(val gameService: GameService,
                    val testService: TestService) {

    companion object {
        const val BASE_URL = "/api/game"
    }

    @GetMapping
    fun getGame(): ResponseEntity<Game?> {
        gameService.game?.let {
            return ResponseEntity.ok(it)
        } ?: run {
            return ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createGame(@RequestBody game: Game): ResponseEntity.BodyBuilder {
        gameService.game = game
        testService.initPlayers()
        return ResponseEntity.ok()
    }

    @DeleteMapping
    fun deleteGame() = ResponseEntity.ok(gameService.deleteGame())

    @PostMapping("/red")
    fun joinRed(@RequestBody player: Player) = ResponseEntity.ok(gameService.addRedPlayer(player))

    @PostMapping("/blue")
    fun joinBlue(@RequestBody player: Player) = ResponseEntity.ok(gameService.addBluePlayer(player))
}