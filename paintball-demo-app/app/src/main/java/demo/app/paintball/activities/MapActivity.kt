package demo.app.paintball.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import demo.app.paintball.R
import demo.app.paintball.map.model.Background
import demo.app.paintball.util.ScalingDetector
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity(), ScalingDetector.ScalingListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mapView.setOnTouchListener(
            ScalingDetector(scalingListener = this)
        )
    }

    override fun onBackPressed() {
        while (true) {
            mapView.setPlayerPosition(Background.playerPosX, Background.playerPosY - 15)
            Thread.sleep(30)
        }
    }

    override fun onScale(scaleFactor: Float) {
        mapView.zoom(scaleFactor)
    }
}