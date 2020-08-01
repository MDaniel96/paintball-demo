package demo.app.paintball.map.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import demo.app.paintball.PaintballApplication
import demo.app.paintball.R

class TeamMate : Dot() {

    override val image: Bitmap =
        BitmapFactory.decodeResource(PaintballApplication.context.resources, R.drawable.blue)

    override var posX = 1485
    override var posY = 4105
}