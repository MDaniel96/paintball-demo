package demo.app.paintball.data.model

class Game {
    var name: String = ""
    var type: String = ""
    var time: Int = 0
    var admin: String = ""
    var redTeam: List<Player>? = null
    var blueTeam: List<Player>? = null
    val playerCnt: Int = 0
}