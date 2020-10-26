package demo.app.paintball.data.mqtt.messages

import demo.app.paintball.data.mqtt.MqttService
import demo.app.paintball.data.mqtt.Topic

class GameMessage {

    var raw: String = ""
    var type: Type = Type.START
    var playerName: String = ""

    companion object {
        private const val SEPARATOR = '|'

        fun parse(raw: String): GameMessage {
            val split = raw.split(SEPARATOR)
            return GameMessage().apply {
                this.raw = raw
                type = Type.find(split[0])
                playerName = split[1]
            }
        }

        fun build(type: Type, playerName: String = "") =
            GameMessage().apply {
                raw = "$type$SEPARATOR$playerName"
                this.type = type
                this.playerName = playerName
            }
    }

    fun publish(mqttService: MqttService) {
        mqttService.publish(Topic.GAME, raw)
    }

    enum class Type(val value: String) {
        START("START"),
        LEAVE("LEAVE");

        companion object {
            fun find(value: String): Type = values().find { it.value == value }!!
        }
    }
}