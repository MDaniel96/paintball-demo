package demo.server.paintball.controller

import demo.server.paintball.data.Game
import demo.server.paintball.service.GameService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(GameController.BASE_URL)
class GameController(val gameService: GameService) {

    companion object {
        const val BASE_URL = "/api/game"
    }

    @GetMapping
    fun getGame(): ResponseEntity<Game> {
        val game = gameService.getGame()
        if (game.name != "") {
            return ResponseEntity.ok(game)
        } else {
            return ResponseEntity.notFound().build()
        }
    }
}