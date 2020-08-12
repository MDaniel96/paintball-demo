package demo.app.paintball.data.mqtt.messages

class PositionMessage {

    var raw: String = ""
    var playerName: String = ""
    var posX: Int = 0
    var posY: Int = 0

    companion object {
        const val SEPARATOR = '|'

        fun parse(raw: String): PositionMessage {
            val split = raw.split(SEPARATOR)
            return PositionMessage().apply {
                this.raw = raw
                playerName = split[0]
                posX = split[1].toInt()
                posY = split[2].toInt()
            }
        }

        fun build(playerName: String, posX: Int, posY: Int) =
            PositionMessage().apply {
                raw = "$playerName$SEPARATOR$posX$SEPARATOR$posY"
                this.playerName = playerName
                this.posX = posX
                this.posY = posY
            }
    }
}