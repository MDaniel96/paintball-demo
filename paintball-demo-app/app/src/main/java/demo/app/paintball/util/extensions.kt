package demo.app.paintball.util

import android.graphics.Canvas
import android.graphics.Color
import android.widget.Toast
import demo.app.paintball.PaintballApplication

fun toast(text: String) {
    Toast.makeText(PaintballApplication.context, text, Toast.LENGTH_SHORT).show()
}

fun Canvas.clear() {
    this.drawColor(Color.DKGRAY)
}

fun Float.toDegree() = Math.toDegrees(this.toDouble()).toFloat()

fun Float.to2PIRadiant() = if (this < 0) (2 * Math.PI + this).toFloat() else this