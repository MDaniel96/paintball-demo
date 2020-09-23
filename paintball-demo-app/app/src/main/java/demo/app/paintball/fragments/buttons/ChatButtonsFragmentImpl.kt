package demo.app.paintball.fragments.buttons

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import demo.app.paintball.PaintballApplication
import demo.app.paintball.R
import demo.app.paintball.activities.MapActivity
import demo.app.paintball.data.mqtt.MqttService
import demo.app.paintball.data.mqtt.messages.ChatMessage
import demo.app.paintball.util.fromHexToByteArray
import demo.app.paintball.util.playAudio
import demo.app.paintball.util.services.ButtonProgressDisplayService
import demo.app.paintball.util.services.PlayerService
import demo.app.paintball.util.services.RecordService
import demo.app.paintball.util.toHexString
import kotlinx.android.synthetic.main.fragment_chat_buttons.*
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule

class ChatButtonsFragmentImpl : ChatButtonsFragment(), MqttService.ChatListener {

    companion object {
        const val RECORDING_TIME = 4_000L
    }

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

    private lateinit var recordService: RecordService

    private var recording = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mqttService = PaintballApplication.services.mqtt().apply { chatListener = this@ChatButtonsFragmentImpl }
        playerService = PaintballApplication.services.player()
        return inflater.inflate(R.layout.fragment_chat_buttons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootLayout = chatButtonsLayout
        btnBottom = fabActivateChat
        btnBottomLayout = fabLayoutActivateChat
        btnBottomTextView = fabTextViewActivateChat
        btnMiddle = fabTeamChat
        btnMiddleLayout = fabLayoutTeamChat
        btnMiddleTextView = fabTextViewTeamChat
        initFabTeamChat()
        requestPermission()
    }

    private fun initFabTeamChat() {
        val rootActivity = activity as Activity
        val buttonProgressDisplayService = ButtonProgressDisplayService(fabTeamChat, rootActivity)
        var timer = Timer()
        val recordingStopped = {
            rootActivity.runOnUiThread {
                timer.cancel()
                val recordedBytes = recordService.stop()
                ChatMessage.build(recordedBytes.toHexString(), playerService.player.name)
                    .publish(mqttService, playerService.player)
                recording = false
                fabTeamChat.setColor(ContextCompat.getColor(PaintballApplication.context, R.color.primaryLightColor))
                fabTeamChat.setIcon(R.drawable.ic_teamspeak, 0)
                buttonProgressDisplayService.stop()
            }
        }
        fabTeamChat.setOnClickListener {
            if (!recording) {
                recordService = RecordService()
                recordService.start()
                recording = true
                fabTeamChat.setColor(ContextCompat.getColor(PaintballApplication.context, R.color.lightTrasparentGray))
                fabTeamChat.setIcon(R.drawable.ic_stop, 0)
                buttonProgressDisplayService.show(RECORDING_TIME)
                timer = Timer()
                timer.schedule(RECORDING_TIME) { recordingStopped() }
            } else {
                recordingStopped()
            }
        }
    }

    override fun chatMessageArrived(message: ChatMessage) {
        if (playerService.player.name != message.playerName) {
            val bytes = message.message.fromHexToByteArray()
            bytes.playAudio()
        }
    }

    private fun requestPermission() {
        // TODO replace permission check with library / extension, when app starts
        // TODO on else and callback branches init media recorder
        if (ContextCompat.checkSelfPermission(activity as MapActivity, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                activity as MapActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(activity as MapActivity, permissions, 0)
        }
    }
}