package demo.app.paintball.data.mqtt.messages

class GameMessage {

    var raw: String = ""
    var type: String = ""
    var playerName: String = ""

    companion object {
        const val SEPARATOR = '|'

        fun parse(raw: String): GameMessage {
            val split = raw.split(SEPARATOR)
            return GameMessage().apply {
                this.raw = raw
                type = split[0]
                playerName = split[1]
            }
        }

        fun build(type: String, playerName: String = "") =
            GameMessage().apply {
                raw = "$type$SEPARATOR$playerName"
                this.type = type
                this.playerName = playerName
            }
    }
}