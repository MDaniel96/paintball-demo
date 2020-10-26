package demo.server.paintball.controller.test

import demo.server.paintball.service.test.TestService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(MqttTestController.BASE_URL)
class MqttTestController(val testService: TestService) {

    companion object {
        const val BASE_URL = "/api/mqtt/test"
    }

    @PostMapping("/positions/start")
    fun sendPositionMessages() {
        testService.sendPositionMessages(0, 3000, -1500, 1500)
    }

    @PostMapping("/positions/stop")
    fun stopPositionMessages() {
        testService.stopPositionMessages()
    }

    @PostMapping("/leave-game/{playerName}")
    fun sendLeaveGameMessage(@PathVariable playerName: String) {
        testService.sendLeaveGameMessage(playerName)
    }
}