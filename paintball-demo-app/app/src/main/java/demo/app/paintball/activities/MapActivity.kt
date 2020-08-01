package demo.app.paintball.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import demo.app.paintball.PaintballApplication
import demo.app.paintball.R
import demo.app.paintball.data.model.Game
import demo.app.paintball.data.model.Player
import demo.app.paintball.data.mqtt.MqttService
import demo.app.paintball.data.rest.RestService
import demo.app.paintball.map.model.Map
import demo.app.paintball.map.sensors.Gyroscope
import demo.app.paintball.map.sensors.ScaleSensor
import demo.app.paintball.util.ErrorHandler
import demo.app.paintball.util.services.PlayerService
import demo.app.paintball.util.toDegree
import demo.app.paintball.util.toast
import kotlinx.android.synthetic.main.activity_map.*
import org.eclipse.paho.client.mqttv3.MqttMessage
import retrofit2.Response
import javax.inject.Inject

class MapActivity : AppCompatActivity(), ScaleSensor.ScaleListener, Gyroscope.GyroscopeListener,
    RestService.SuccessListener, MqttService.SuccessListener {

    @Inject
    lateinit var restService: RestService

    @Inject
    lateinit var mqttService: MqttService

    @Inject
    lateinit var playerService: PlayerService

    private lateinit var gyroscope: Gyroscope

    private var game: Game? = null

    private lateinit var player: Player

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

        playerService = PaintballApplication.services.player()
        restService = PaintballApplication.services.rest().apply {
            listener = this@MapActivity
            errorListener = ErrorHandler
        }
        mqttService = PaintballApplication.services.mqtt().apply {
            listener = this@MapActivity
        }

        restService.getGame()
        player = playerService.player
        toast(player.name + " " + player.team)
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

    override fun getGameSuccess(response: Response<Game>) {
        game = response.body()
    }

    override fun createGameSuccess() {
    }

    override fun addRedPlayerSuccess() {
    }

    override fun addBluePlayerSuccess() {
    }

    override fun connectComplete() {
    }

    override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
    }
}