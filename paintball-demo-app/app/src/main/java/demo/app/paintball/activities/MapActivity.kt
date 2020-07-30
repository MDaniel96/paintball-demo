package demo.app.paintball.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import demo.app.paintball.R
import demo.app.paintball.map.model.Map
import demo.app.paintball.map.sensors.Gyroscope
import demo.app.paintball.map.sensors.ScaleSensor
import demo.app.paintball.util.toDegree
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity(), ScaleSensor.ScaleListener,
    Gyroscope.GyroscopeListener {

    private lateinit var gyroscope: Gyroscope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mapView.setOnTouchListener(
            ScaleSensor(scaleListener = this)
        )
        gyroscope = Gyroscope(
            gyroscopeListener = this
        )
    }

    override fun onResume() {
        super.onResume()
        gyroscope.start()
    }

    override fun onPause() {
        super.onPause()
        gyroscope.stop()
    }

    override fun onBackPressed() {
        while (true) {
            mapView.setPlayerPosition(Map.playerPosX + 15, Map.playerPosY)
            Thread.sleep(30)
        }
    }

    override fun onScaleChanged(scaleFactor: Float) {
        mapView.zoom(scaleFactor)
    }

    override fun onOrientationChanged(radian: Float) {
        mapView.setPlayerOrientation(radian.toDegree())
    }
}