package demo.server.paintball.service.test

interface TestService {

    fun initPlayers()

    fun sendPositionMessages(blueStartX: Int, redStartX: Int, rangeFrom: Long, rangeTo: Long)

    fun stopPositionMessages()

    fun sendLeaveGameMessage(playerName: String)

    fun sendChatMessage(playerName: String)
}