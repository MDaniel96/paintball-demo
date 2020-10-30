package demo.app.paintball.config.map

import demo.app.paintball.PaintballApplication.Companion.context
import demo.app.paintball.R

abstract class MapConfig {

    companion object {
        fun create() = when (context.getString(R.string.map)) {
            "garden" -> GardenMapConfig()
            "gyenes" -> GyenesMapConfig()
            "test" -> TestMapConfig()
            else -> GyenesMapConfig()
        }
    }

    /*
    Map's image
     */
    abstract val imageDrawableId: Int

    /*
    Width of image in pixels
     */
    abstract val imageWidthPx: Int

    /*
    Maps forward orientation in degrees from north (e.g.: west is 270)
     */
    abstract val mapOrientation: Int

    /*
    Zoom limits of map
     */
    abstract val minZoom: Double
    abstract val maxZoom: Double

    /*
    Anchor0's (x,y) position in mm from top left corner of image
     */
    abstract val anchorOriginPosX: Int
    abstract val anchorOriginPosY: Int

    /*
    Anchor0's x position in pixels from left edge of image
     */
    abstract val anchorOriginPxX: Int

    /*
    Positions (x, y, z) of anchors
     */
    abstract val anchors: List<IntArray>
}