package demo.app.paintball.fragments.buttons

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import demo.app.paintball.PaintballApplication
import demo.app.paintball.R
import demo.app.paintball.util.FabProgressDisplayer
import demo.app.paintball.util.toast
import kotlinx.android.synthetic.main.fragment_chat_buttons.*
import java.util.*
import kotlin.concurrent.schedule


class ChatButtonsFragmentImpl : ChatButtonsFragment() {

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

    private var recording = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
    }

    private fun initFabTeamChat() {
        val rootActivity = activity as Activity
        val fabProgressDisplayer = FabProgressDisplayer(fabTeamChat, rootActivity)
        var timer = Timer()
        val stop = {
            rootActivity.runOnUiThread {
                stopRecording()
                recording = false
                timer.cancel()
                fabTeamChat.setColor(ContextCompat.getColor(PaintballApplication.context, R.color.primaryLightColor))
                fabTeamChat.setIcon(R.drawable.ic_teamspeak, 0)
                fabProgressDisplayer.stop()
            }
        }
        fabTeamChat.setOnClickListener {
            if (!recording) {
                startRecording()
                recording = true
                fabTeamChat.setColor(ContextCompat.getColor(PaintballApplication.context, R.color.lightTrasparentGray))
                fabTeamChat.setIcon(R.drawable.ic_stop, 0)
                fabProgressDisplayer.show(RECORDING_TIME)
                timer = Timer()
                timer.schedule(RECORDING_TIME) { stop() }
            } else {
                stop()
            }
        }
    }

    private fun startRecording() {
        toast("Started recording")
    }

    private fun stopRecording() {
        toast("Stopped recording")
    }
}