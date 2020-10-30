package demo.server.paintball.data

class Player(
        var name: String = "",
        var deviceName: String = "",
        var team: Team? = null,
        var isAdmin: Boolean = false
) {

    enum class Team(val value: String) {
        RED("RED"),
        BLUE("BLUE");
    }
}