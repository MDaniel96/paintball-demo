package demo.app.paintball.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import demo.app.paintball.PaintballApplication
import demo.app.paintball.R
import demo.app.paintball.data.mqtt.MqttService
import demo.app.paintball.data.mqtt.messages.PositionMessage
import demo.app.paintball.data.rest.RestService
import demo.app.paintball.data.rest.models.Game
import demo.app.paintball.fragments.buttons.ChatButtonsFragment
import demo.app.paintball.fragments.buttons.MainButtonsFragment
import demo.app.paintball.map.MapView
import demo.app.paintball.map.rendering.MapViewImpl
import demo.app.paintball.map.sensors.GestureSensor
import demo.app.paintball.map.sensors.Gyroscope
import demo.app.paintball.util.ErrorHandler
import demo.app.paintball.util.getTeamChatTopic
import demo.app.paintball.util.getTeamPositionsTopic
import demo.app.paintball.util.services.PlayerService
import demo.app.paintball.util.toDegree
import kotlinx.android.synthetic.main.activity_map.*
import retrofit2.Response
import javax.inject.Inject


class MapActivity : AppCompatActivity(), GestureSensor.GestureListener, Gyroscope.GyroscopeListener, RestService.SuccessListener,
    MqttService.PositionListener, MapViewImpl.MapViewCreatedListener {

    @Inject
    lateinit var restService: RestService

    @Inject
    lateinit var mqttService: MqttService

    @Inject
    lateinit var playerService: PlayerService

    private var game: Game? = null
    private var isMapButtonsOpen = false

    private lateinit var map: MapView
    private lateinit var mainButtons: MainButtonsFragment
    private lateinit var chatButtons: ChatButtonsFragment

    private lateinit var gyroscope: Gyroscope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        map = mapView
        mainButtons = supportFragmentManager.findFragmentById(R.id.mainButtonsFragment) as MainButtonsFragment
        chatButtons = supportFragmentManager.findFragmentById(R.id.chatButtonsFragment) as ChatButtonsFragment

        mainButtons.initLevel(0)
        chatButtons.initLevel(1)

        map.setOnTouchListener(GestureSensor(gestureListener = this, scrollPanel = buttonsPanel))
        gyroscope = Gyroscope(gyroscopeListener = this)

        playerService = PaintballApplication.services.player()
        restService = PaintballApplication.services.rest().apply { listener = this@MapActivity; errorListener = ErrorHandler }
        mqttService = PaintballApplication.services.mqtt().apply { positionListener = this@MapActivity }

        restService.getGame()
        mqttService.subscribe(playerService.player.getTeamChatTopic())
        mqttService.subscribe(playerService.player.getTeamPositionsTopic())

        fabActivateButtons.setOnClickListener {
            if (isMapButtonsOpen) hideButtons() else showButtons()
        }
    }

    override fun onResume() {
        super.onResume()
        gyroscope.start()
    }

    override fun onPause() {
        super.onPause()
        gyroscope.stop()
    }

    override fun mapViewCreated() {
        if (resources.getBoolean(R.bool.displayAnchors)) {
            addAnchorsToMap()
        }
    }

    override fun onBackPressed() {
    }

    override fun onScaleChanged(scaleFactor: Float) {
        map.zoom(scaleFactor)
    }

    override fun onZoomIn() {
        hideButtons()
    }

    override fun onZoomOut() {
        gameDetailLayout.animate().translationX(0F)
    }

    override fun onScrollUp() {
        mainButtons.changeLevel(-1)
        chatButtons.changeLevel(0)
    }

    override fun onScrollDown() {
        mainButtons.changeLevel(0)
        chatButtons.changeLevel(1)
    }

    override fun onOrientationChanged(radian: Float) {
        map.setPlayerOrientation(radian.toDegree())
    }

    override fun getGameSuccess(response: Response<Game>) {
        game = response.body()
        addPlayersToMap()
    }

    private fun addPlayersToMap() {
        game?.redTeam
            ?.filter { it.name != playerService.player.name }
            ?.forEach { map.addRedPlayer(it.name) }
        game?.blueTeam
            ?.filter { it.name != playerService.player.name }
            ?.forEach { map.addBluePlayer(it.name) }
    }

    override fun createGameSuccess() {
    }

    override fun addRedPlayerSuccess() {
    }

    override fun addBluePlayerSuccess() {
    }

    override fun positionMessageArrived(message: PositionMessage) {
        map.setMovablePosition(message.playerName, message.posX, message.posY)
    }

    private fun showButtons() {
        isMapButtonsOpen = true
        fabActivateButtons.setImageDrawable(
            ResourcesCompat.getDrawable(resources, R.drawable.ic_unfold_less, null)
        )
        fabActivateButtons.animate().rotation(180F)
        gameDetailLayout.animate().translationX(0F)

        mainButtons.show()
        chatButtons.show()
    }

    private fun hideButtons() {
        isMapButtonsOpen = false
        fabActivateButtons.setImageDrawable(
            ResourcesCompat.getDrawable(resources, R.drawable.ic_unfold_more, null)
        )
        fabActivateButtons.animate().rotation(-180F)
        gameDetailLayout.animate().translationX(-300F)

        mainButtons.hide()
        chatButtons.hide()
    }

    private fun addAnchorsToMap() {
        // TODO: get this info from backend or config file
        val anchors = listOf(
            intArrayOf(0, 0, 1100),
            intArrayOf(3800, 0, 1100),
            intArrayOf(3800, 4100, 1100),
            intArrayOf(0, 4100, 1100)
        )
        anchors.forEach { map.addAnchor(it[0], it[1]) }
    }
}