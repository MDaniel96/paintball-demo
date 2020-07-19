package demo.server.paintball.data

data class Player(
        var name: String,
        var deviceName: String,
        var posX: Float,
        var posY: Float,
        var isAdmin: Boolean
)