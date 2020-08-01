package demo.app.paintball.map.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import demo.app.paintball.PaintballApplication
import demo.app.paintball.R

class Enemy : Dot() {

    override val image: Bitmap =
        BitmapFactory.decodeResource(PaintballApplication.context.resources, R.drawable.red)

    override var posX = 4185
    override var posY = 4105
}