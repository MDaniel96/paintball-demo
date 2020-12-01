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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Player
        if (name != other.name) return false
        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}