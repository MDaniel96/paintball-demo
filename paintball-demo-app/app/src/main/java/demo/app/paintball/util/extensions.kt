package demo.app.paintball.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaPlayer
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import demo.app.paintball.PaintballApplication.Companion.context
import demo.app.paintball.data.mqtt.Topic
import demo.app.paintball.data.rest.models.Player
import java.io.File

fun toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(context, text, duration).show()
}

fun Canvas.clear() {
    this.drawColor(Color.DKGRAY)
}

// ====================================
//  MATH
// ====================================

fun Float.toDegree() = Math.toDegrees(this.toDouble()).toFloat()

fun Float.to2PIRadiant() = if (this < 0) (2 * Math.PI + this).toFloat() else this

// ====================================
//  TOPICS
// ====================================

// TODO: refactor: create Team class -> RedTeam, Blueteam subclasses. store these infos in them
fun Player.getTeamPositionsTopic() = when (team) {
    "RED" -> Topic.POSITIONS_RED_TEAM
    "BLUE" -> Topic.POSITIONS_BLUE_TEAM
    else -> Topic.POSITIONS_BLUE_TEAM
}

fun Player.getEnemyPositionsTopic() = when (team) {
    "RED" -> Topic.POSITIONS_BLUE_TEAM
    "BLUE" -> Topic.POSITIONS_RED_TEAM
    else -> Topic.POSITIONS_BLUE_TEAM
}

fun Player.getTeamChatTopic() = when (team) {
    "RED" -> Topic.CHAT_RED_TEAM
    "BLUE" -> Topic.CHAT_BLUE_TEAM
    else -> Topic.CHAT_BLUE_TEAM
}

fun Player.getEnemyChatTopic() = when (team) {
    "RED" -> Topic.CHAT_RED_TEAM
    "BLUE" -> Topic.CHAT_BLUE_TEAM
    else -> Topic.CHAT_BLUE_TEAM
}

// ====================================
//  AUDIO
// ====================================

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

fun String.fromHexToByteArray() = this.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

fun ByteArray.playAudio() {
    val filePath = context.getCacheDir().toString() + "/teamChat.3gp"
    File(filePath).run {
        writeBytes(this@playAudio)
    }
    MediaPlayer().run {
        setDataSource(filePath)
        prepare()
        start()
        setOnCompletionListener {
            it.reset()
            it.release()
        }
    }
}

// ====================================
//  PERMISSIONS
// ====================================

fun Activity.checkPermissions(permissions: List<String>) {
    permissions.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
        .map { ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0) }
}