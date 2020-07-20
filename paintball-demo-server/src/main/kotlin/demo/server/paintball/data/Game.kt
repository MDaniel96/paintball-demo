package demo.server.paintball.data

object Game {
    var name: String = ""
    var type: String = ""
    var time: Int = 0
    var admin: String = ""
    var redTeam: List<Player>? = null
    var blueTeam: List<Player>? = null

    val playerCnt: Int
        get() {
            return if (redTeam != null && blueTeam != null) {
                redTeam!!.size + blueTeam!!.size
            } else {
                0
            }
        }
}