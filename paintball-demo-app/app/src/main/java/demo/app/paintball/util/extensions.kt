package demo.app.paintball.util

import android.graphics.Canvas
import android.graphics.Color
import android.widget.Toast
import demo.app.paintball.PaintballApplication
import demo.app.paintball.data.model.Player
import demo.app.paintball.data.mqtt.Topic

fun toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(PaintballApplication.context, text, duration).show()
}

fun Canvas.clear() {
    this.drawColor(Color.DKGRAY)
}

fun Float.toDegree() = Math.toDegrees(this.toDouble()).toFloat()

fun Float.to2PIRadiant() = if (this < 0) (2 * Math.PI + this).toFloat() else this

fun Player.getTeamTopic() = when (this.team) {
    "RED" -> Topic.RED_TEAM
    "BLUE" -> Topic.BLUE_TEAM
    else -> Topic.BLUE_TEAM
}

fun Player.getEnemyTopic() = when (this.team) {
    "RED" -> Topic.BLUE_TEAM
    "BLUE" -> Topic.RED_TEAM
    else -> Topic.BLUE_TEAM
}