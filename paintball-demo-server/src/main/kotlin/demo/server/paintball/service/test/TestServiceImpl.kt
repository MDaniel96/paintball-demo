package demo.server.paintball.service.test

import demo.server.paintball.config.AppConfig
import demo.server.paintball.data.Player
import demo.server.paintball.service.GameService
import demo.server.paintball.service.MqttService
import org.springframework.stereotype.Service
import java.util.*
import kotlin.concurrent.schedule
import kotlin.random.Random

@Service
class TestServiceImpl(val gameService: GameService,
                      val mqttService: MqttService,
                      val appConfig: AppConfig) : TestService {

    companion object {
        const val GAME = "game"
        const val RED_TEAM_TOPIC = "positions/redTeam"
        const val BLUE_TEAM_TOPIC = "positions/blueTeam"
    }

    private lateinit var timer: Timer

    override fun initPlayers() {
        if (appConfig.environment == "test") {
            val redPlayer1 = Player().apply { name = "Dani" }
            val redPlayer2 = Player().apply { name = "Akos" }
            val redPlayer3 = Player().apply { name = "Porgi" }
            val bluePlayer1 = Player().apply { name = "Mate" }
            val bluePlayer2 = Player().apply { name = "Markolt" }

            gameService.game?.redTeam?.addAll(
                    mutableListOf(redPlayer1, redPlayer2, redPlayer3)
            )
            gameService.game?.blueTeam?.addAll(
                    mutableListOf(bluePlayer1, bluePlayer2)
            )
        }
    }

    override fun sendPositionMessages(blueStartX: Int, redStartX: Int, rangeFrom: Long, rangeTo: Long) {
        timer = Timer()
        if (appConfig.environment == "test") {
            val redTeam = gameService.game?.redTeam
            val blueTeam = gameService.game?.blueTeam
            redTeam?.filter { !it.isAdmin }?.forEach {
                val testPositions = getPositions(it.name, blueStartX + Random.nextLong(rangeFrom, rangeTo).toInt(),
                        0 + Random.nextLong(rangeFrom, rangeTo).toInt(), 10)
                var i = 0
                timer.schedule(40L, Random.nextLong(50, 150)) {
                    if (i < testPositions.size - 1) i++ else i = 0
                    mqttService.publish(
                            topic = RED_TEAM_TOPIC,
                            message = testPositions[i]
                    )
                }
            }
            blueTeam?.filter { !it.isAdmin }?.forEach {
                val testPositions = getPositions(it.name, blueStartX + Random.nextLong(rangeFrom, rangeTo).toInt(),
                        4000 + Random.nextLong(rangeFrom, rangeTo).toInt(), 10)
                var i = 0
                timer.schedule(40L, Random.nextLong(50, 150)) {
                    if (i < testPositions.size - 1) i++ else i = 0
                    mqttService.publish(
                            topic = BLUE_TEAM_TOPIC,
                            message = testPositions[i]
                    )
                }
            }
        }

    }

    override fun stopPositionMessages() {
        timer.cancel()
    }

    override fun sendLeaveGameMessage(playerName: String) {
        mqttService.publish(topic = GAME, message = "LEAVE|$playerName")
    }

    private fun getPositions(playerName: String, startX: Int, startY: Int, step: Int): List<String> {
        val positions = mutableListOf<String>()
        val randomRange = Random.nextInt(200, 400)
        var startX_ = startX
        var startY_ = startY

        for (pos in startX..startX + randomRange step step) {
            positions.add("$playerName|$pos|$startY")
            startX_ = pos
        }
        for (pos in startY..startY + randomRange step step) {
            positions.add("$playerName|$startX_|$pos")
            startY_ = pos
        }
        for (pos in startX_ downTo startX step step) {
            positions.add("$playerName|$pos|$startY_")
        }
        for (pos in startY_ downTo startY step step) {
            positions.add("$playerName|$startX|$pos")
        }
        return positions
    }
}