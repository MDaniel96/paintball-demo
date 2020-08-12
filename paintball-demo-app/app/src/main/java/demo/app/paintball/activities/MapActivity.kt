package demo.app.paintball.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import demo.app.paintball.PaintballApplication
import demo.app.paintball.R
import demo.app.paintball.data.model.Game
import demo.app.paintball.data.mqtt.MqttService
import demo.app.paintball.data.mqtt.Topic
import demo.app.paintball.data.mqtt.messages.PositionMessage
import demo.app.paintball.data.rest.RestService
import demo.app.paintball.map.renderables.Map
import demo.app.paintball.map.rendering.MapView
import demo.app.paintball.map.sensors.Gyroscope
import demo.app.paintball.map.sensors.ScaleSensor
import demo.app.paintball.util.ErrorHandler
import demo.app.paintball.util.services.PlayerService
import demo.app.paintball.util.toDegree
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

    private var game: Game? = null

    private lateinit var gyroscope: Gyroscope

    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        map = mapView
        map.setOnTouchListener(ScaleSensor(scaleListener = this))
        gyroscope = Gyroscope(gyroscopeListener = this)

        playerService = PaintballApplication.services.player()
        restService = PaintballApplication.services.rest().apply {
            listener = this@MapActivity
            errorListener = ErrorHandler
        }
        mqttService = PaintballApplication.services.mqtt().apply {
            listener = this@MapActivity
        }

        restService.getGame()
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
            map.setPlayerPosition(Map.playerPosX + 15, Map.playerPosY)
            Thread.sleep(30)
        }
    }

    override fun onScaleChanged(scaleFactor: Float) {
        map.zoom(scaleFactor)
    }

    override fun onOrientationChanged(radian: Float) {
        map.setPlayerOrientation(radian.toDegree())
    }

    override fun getGameSuccess(response: Response<Game>) {
        game = response.body()
        addPlayersToMap()
        mqttService.subscribe(getTeamTopic())
    }

    private fun addPlayersToMap() {
        game?.redTeam
            ?.filter { it.name != playerService.player.name }
            ?.forEach { map.addRedPlayer(it.name) }
        game?.blueTeam
            ?.filter { it.name != playerService.player.name }
            ?.forEach { map.addBluePlayer(it.name) }
    }

    private fun getTeamTopic() = when (playerService.player.team) {
        "RED" -> Topic.RED_TEAM
        "BLUE" -> Topic.BLUE_TEAM
        else -> Topic.BLUE_TEAM
    }

    override fun createGameSuccess() {
    }

    override fun addRedPlayerSuccess() {
    }

    override fun addBluePlayerSuccess() {
    }

    override fun connectComplete() {
    }

    override fun messageArrived(topic: Topic, mqttMessage: MqttMessage) {
        when (topic) {
            Topic.RED_TEAM, Topic.BLUE_TEAM -> {
                val message = PositionMessage.parse(mqttMessage.toString())
                map.setDotPosition(message.playerName, message.posX, message.posY)
            }
            Topic.GAME -> {
            }
        }
    }
}