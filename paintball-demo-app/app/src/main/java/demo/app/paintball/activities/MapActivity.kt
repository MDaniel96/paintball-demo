package demo.app.paintball.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import demo.app.paintball.PaintballApplication
import demo.app.paintball.PaintballApplication.Companion.context
import demo.app.paintball.R
import demo.app.paintball.data.model.Game
import demo.app.paintball.data.mqtt.MqttService
import demo.app.paintball.data.mqtt.Topic
import demo.app.paintball.data.mqtt.messages.PositionMessage
import demo.app.paintball.data.rest.RestService
import demo.app.paintball.map.renderables.Map
import demo.app.paintball.map.rendering.MapView
import demo.app.paintball.map.sensors.GestureSensor
import demo.app.paintball.map.sensors.Gyroscope
import demo.app.paintball.util.*
import demo.app.paintball.util.services.PlayerService
import kotlinx.android.synthetic.main.activity_map.*
import org.eclipse.paho.client.mqttv3.MqttMessage
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule


class MapActivity : AppCompatActivity(), GestureSensor.GestureListener, Gyroscope.GyroscopeListener,
    RestService.SuccessListener, MqttService.SuccessListener {

    companion object {
        const val SPYING_TIME = 7_000L
        const val SPYING_RECHARGE_TIME = 12_000L
    }

    @Inject
    lateinit var restService: RestService

    @Inject
    lateinit var mqttService: MqttService

    @Inject
    lateinit var playerService: PlayerService

    private var game: Game? = null
    private var isFabOpen = false

    private var screenHeight = 0F
    private var mainButtonsPanelTranslate = 0F
    private var chatButtonsPanelTranslate = 0F

    private lateinit var gyroscope: Gyroscope
    private lateinit var map: MapView
    private lateinit var fabProgressDisplayer: FabProgressDisplayer

    private val timer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        screenHeight = resources.displayMetrics.heightPixels.toFloat()
        chatButtonsPanelTranslate = screenHeight

        map = mapView
        map.setOnTouchListener(GestureSensor(gestureListener = this, scrollPanel = buttonsPanel))
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

        fabActivateButtons.setOnClickListener {
            if (!isFabOpen) {
                showButtons()
            } else {
                hideButtons()
            }
        }

        fabProgressDisplayer = FabProgressDisplayer(fabSpying, this)
        fabSpying.setOnClickListener {
            mqttService.subscribe(playerService.player.getEnemyTopic())
            fabSpying.isEnabled = false
            fabSpying.setColor(ContextCompat.getColor(context, R.color.lightTrasparentGray))

            timer.schedule(SPYING_TIME) {
                runOnUiThread {
                    mqttService.unsubscribe(playerService.player.getEnemyTopic())
                    fabProgressDisplayer.show(SPYING_RECHARGE_TIME)
                }
            }
            timer.schedule(SPYING_TIME + SPYING_RECHARGE_TIME) {
                runOnUiThread {
                    fabSpying.isEnabled = true
                    fabSpying.setColor(ContextCompat.getColor(context, R.color.primaryLightColor))
                }
            }
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

    override fun onBackPressed() {
        while (true) {
            map.setPlayerPosition(Map.playerPosX + 15, Map.playerPosY)
            Thread.sleep(30)
        }
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
        if (isFabOpen) {
            mainButtonsPanelTranslate = -screenHeight
            chatButtonsPanelTranslate = 0F

            mainButtonsLayout.animate().translationY(mainButtonsPanelTranslate)
            chatButtonsLayout.animate().translationY(chatButtonsPanelTranslate)
        }
    }

    override fun onScrollDown() {
        if (isFabOpen) {
            mainButtonsPanelTranslate = 0F
            chatButtonsPanelTranslate = screenHeight

            mainButtonsLayout.animate().translationY(mainButtonsPanelTranslate)
            chatButtonsLayout.animate().translationY(chatButtonsPanelTranslate)
        }
    }

    override fun onOrientationChanged(radian: Float) {
        map.setPlayerOrientation(radian.toDegree())
    }

    override fun getGameSuccess(response: Response<Game>) {
        game = response.body()
        addPlayersToMap()
        mqttService.subscribe(playerService.player.getTeamTopic())
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

    private fun showButtons() {
        isFabOpen = true
        fabActivateButtons.setImageDrawable(
            ResourcesCompat.getDrawable(resources, R.drawable.ic_unfold_less, null)
        )
        fabActivateButtons.animate().rotation(180F)
        gameDetailLayout.animate().translationX(0F)

        // Main Buttons
        mainButtonsLayout.animate().translationY(mainButtonsPanelTranslate)

        fabLayoutLeaveGame.animate()
            .translationY(-resources.getDimension(R.dimen.fab_leave_game_translate))
        fabLeaveGame.animate().rotation(0F)
        fabTextViewLeaveGame.animate().alpha(1F).duration = 600

        fabLayoutSpying.animate()
            .translationY(-resources.getDimension(R.dimen.fab_spying_translate))
        fabSpying.animate().rotation(0F)
        fabTextViewSpying.animate().alpha(1F).duration = 600

        // Chat buttons
        chatButtonsLayout.animate().translationY(chatButtonsPanelTranslate)

        fabLayoutActivateChat.animate()
            .translationY(-resources.getDimension(R.dimen.fab_leave_game_translate))
        fabActivateChat.animate().rotation(0F)
        fabTextViewActivateChat.animate().alpha(1F).duration = 600

        fabLayoutTeamChat.animate()
            .translationY(-resources.getDimension(R.dimen.fab_spying_translate))
        fabTeamChat.animate().rotation(0F)
        fabTextViewTeamChat.animate().alpha(1F).duration = 600
    }

    private fun hideButtons() {
        isFabOpen = false
        fabActivateButtons.setImageDrawable(
            ResourcesCompat.getDrawable(resources, R.drawable.ic_unfold_more, null)
        )
        fabActivateButtons.animate().rotation(-180F)
        gameDetailLayout.animate().translationX(-300F)

        // Main Buttons
        mainButtonsLayout.animate().translationY(0F)

        fabLayoutLeaveGame.animate().translationY(0F)
        fabLeaveGame.animate().rotation(-120F)
        fabTextViewLeaveGame.animate().alpha(0F).duration = 300

        fabLayoutSpying.animate().translationY(0F)
        fabSpying.animate().rotation(-120F)
        fabTextViewSpying.animate().alpha(0F).duration = 300

        // Chat buttons
        chatButtonsLayout.animate().translationY(0F)

        fabLayoutActivateChat.animate().translationY(0F)
        fabActivateChat.animate().rotation(-120F)
        fabTextViewActivateChat.animate().alpha(0F).duration = 300

        fabLayoutTeamChat.animate().translationY(0F)
        fabTeamChat.animate().rotation(-120F)
        fabTextViewTeamChat.animate().alpha(0F).duration = 300
    }
}