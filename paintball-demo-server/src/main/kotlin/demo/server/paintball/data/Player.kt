package demo.server.paintball.data

class Player(
        var name: String = "",
        var deviceName: String = "",
        var posX: Float = 0F,
        var posY: Float = 0F,
        var isAdmin: Boolean = false
)