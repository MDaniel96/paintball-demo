package demo.app.paintball.util

import android.widget.Toast
import demo.app.paintball.PaintballApplication

fun toast(text: String) {
    Toast.makeText(PaintballApplication.context, text, Toast.LENGTH_SHORT).show()
}