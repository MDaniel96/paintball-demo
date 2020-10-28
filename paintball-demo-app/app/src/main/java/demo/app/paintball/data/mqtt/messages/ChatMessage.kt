package demo.app.paintball.data.mqtt.messages

import demo.app.paintball.PaintballApplication.Companion.player
import demo.app.paintball.data.mqtt.MqttMessage
import demo.app.paintball.util.getTeamChatTopic

class ChatMessage(raw: String) : MqttMessage(raw) {

    var message = rawFields[1]
    var length = rawFields[2].toLong()

    constructor(message: String, length: Long) : this("${player.name}$SEPARATOR$message$SEPARATOR$length")

    override fun getTopic() = player.getTeamChatTopic()
}