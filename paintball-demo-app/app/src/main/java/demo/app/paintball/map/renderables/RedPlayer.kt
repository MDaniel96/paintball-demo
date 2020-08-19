package demo.app.paintball.map.renderables

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import demo.app.paintball.PaintballApplication
import demo.app.paintball.R

class RedPlayer(name: String) : Dot(name) {

    override val image: Bitmap =
        BitmapFactory.decodeResource(
            PaintballApplication.context.resources,
            R.drawable.ic_red_player
        )

    override var posX = 6185
    override var posY = 3105
}