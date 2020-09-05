package demo.app.paintball.fragments.buttons

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import demo.app.paintball.PaintballApplication
import demo.app.paintball.R
import demo.app.paintball.activities.MapActivity
import demo.app.paintball.data.mqtt.MqttService
import demo.app.paintball.util.FabProgressDisplayer
import demo.app.paintball.util.getEnemyTopic
import demo.app.paintball.util.services.PlayerService
import kotlinx.android.synthetic.main.fragment_main_buttons.*
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

class MainButtonsFragmentImpl : MainButtonsFragment() {

    override lateinit var rootLayout: View

    override lateinit var btnBottom: View
    override lateinit var btnBottomLayout: View
    override lateinit var btnBottomTextView: View

    override lateinit var btnMiddle: View
    override lateinit var btnMiddleLayout: View
    override lateinit var btnMiddleTextView: View

    @Inject
    lateinit var mqttService: MqttService

    @Inject
    lateinit var playerService: PlayerService

    private val timer = Timer()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        playerService = PaintballApplication.services.player()
        mqttService = PaintballApplication.services.mqtt().apply { listener = activity as MapActivity }
        return inflater.inflate(R.layout.fragment_main_buttons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFabSpyingButton()
        rootLayout = mainButtonsLayout
        btnBottom = fabLeaveGame
        btnBottomLayout = fabLayoutLeaveGame
        btnBottomTextView = fabTextViewLeaveGame
        btnMiddle = fabSpying
        btnMiddleLayout = fabLayoutSpying
        btnMiddleTextView = fabTextViewSpying
    }

    private fun initFabSpyingButton() {
        val rootActivity = activity as Activity
        val fabProgressDisplayer = FabProgressDisplayer(fabSpying, rootActivity)
        fabSpying.setOnClickListener {
            mqttService.subscribe(playerService.player.getEnemyTopic())
            fabSpying.isEnabled = false
            fabSpying.setColor(ContextCompat.getColor(PaintballApplication.context, R.color.lightTrasparentGray))

            timer.schedule(MapActivity.SPYING_TIME) {
                rootActivity.runOnUiThread {
                    mqttService.unsubscribe(playerService.player.getEnemyTopic())
                    fabProgressDisplayer.show(MapActivity.SPYING_RECHARGE_TIME)
                }
            }
            timer.schedule(MapActivity.SPYING_TIME + MapActivity.SPYING_RECHARGE_TIME) {
                rootActivity.runOnUiThread {
                    fabSpying.isEnabled = true
                    fabSpying.setColor(ContextCompat.getColor(PaintballApplication.context, R.color.primaryLightColor))
                }
            }
        }
    }
}