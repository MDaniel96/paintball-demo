package demo.server.paintball.controller

import demo.server.paintball.config.AppConfig
import demo.server.paintball.mock.MockService
import demo.server.paintball.service.GameService
import demo.server.paintball.service.MqttService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import kotlin.concurrent.schedule
import kotlin.random.Random

@RestController
@RequestMapping(MqttController.BASE_URL)
class MqttController(val mqttService: MqttService,
                     val mockService: MockService,
                     val appConfig: AppConfig) {

    companion object {
        const val BASE_URL = "/api/mqtt"
        const val RED_TEAM_TOPIC = "positions/redTeam"
        const val BLUE_TEAM_TOPIC = "positions/blueTeam"
    }

    @PostMapping("test/positions")
    fun sendTestPositions() {
        if (appConfig.environment == "test") {
            val redTeam = GameService.game?.redTeam
            val blueTeam = GameService.game?.blueTeam
            redTeam?.filter { !it.isAdmin }?.forEach {
                val testPositions = mockService.getTestPositions(it.name, 4000, 4000, 10)
                var i = 0
                Timer().schedule(0, Random.nextLong(50, 150)) {
                    mqttService.publish(
                            topic = RED_TEAM_TOPIC,
                            message = testPositions[i++]
                    )
                }
            }
            blueTeam?.filter { !it.isAdmin }?.forEach {
                val testPositions = mockService.getTestPositions(it.name, 2000, 4000, 10)
                var i = 0
                Timer().schedule(0, Random.nextLong(50, 150)) {
                    mqttService.publish(
                            topic = BLUE_TEAM_TOPIC,
                            message = testPositions[i++]
                    )
                }
            }
        }
    }
}