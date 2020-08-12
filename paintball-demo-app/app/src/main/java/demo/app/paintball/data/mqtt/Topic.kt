package demo.app.paintball.data.mqtt

enum class Topic(val value: String) {
    GAME("game"),
    RED_TEAM("positions/redTeam"),
    BLUE_TEAM("positions/blueTeam");

    companion object {
        fun find(value: String): Topic = values().find { it.value == value }!!
    }
}