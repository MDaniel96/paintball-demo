package demo.app.paintball.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import demo.app.paintball.PaintballApplication.Companion.player
import demo.app.paintball.PaintballApplication.Companion.services
import demo.app.paintball.R
import demo.app.paintball.data.mqtt.MqttService
import demo.app.paintball.data.mqtt.MqttTopic
import demo.app.paintball.data.mqtt.messages.GameMessage
import demo.app.paintball.data.rest.RestService
import demo.app.paintball.data.rest.models.Game
import demo.app.paintball.fragments.dialogs.ViewPlayersFragment
import demo.app.paintball.util.ErrorHandler
import demo.app.paintball.util.toast
import kotlinx.android.synthetic.main.activity_join_game.*
import retrofit2.Response
import javax.inject.Inject

class JoinGameActivity : AppCompatActivity(), RestService.SuccessListener, MqttService.GameListener {

    @Inject
    lateinit var restService: RestService

    @Inject
    lateinit var mqttService: MqttService

    private var game: Game? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_game)

        restService = services.rest().apply { listener = this@JoinGameActivity; errorListener = ErrorHandler }
        mqttService = services.mqtt().apply { gameListener = this@JoinGameActivity }
        restService.getGame()
    }

    override fun getGameSuccess(response: Response<Game>) {
        if (response.code() == 404) {
            toast("No game found")
        } else {
            game = response.body()
            initTexts()
            initStartGameButton()
            initTeamButtons()
        }
    }

    private fun initTexts() {
        game?.let {
            tvGameName.text = it.name
            tvGameType.text = it.type
            tvGameAdmin.text =
                String.format(getString(R.string.admin_is), it.admin)
            tvGamePlayerCnt.text =
                String.format(getString(R.string.player_cnt), it.playerCnt)
            btnViewRed.text =
                String.format(getString(R.string.view_players_), it.redTeam.size)
            btnViewBlue.text =
                String.format(getString(R.string.view_players_), it.blueTeam.size)
        }
    }

    private fun initStartGameButton() {
        if (!player.isAdmin) {
            btnStartGame.isEnabled = false
            btnStartGame.text = getString(R.string.waiting_for_admin)
        } else {
            btnStartGame.setOnClickListener {
                GameMessage(type = GameMessage.Type.START).publish(mqttService)
            }
        }
    }

    private fun initTeamButtons() {
        btnJoinRed.setOnClickListener {
            if (player.team == null) {
                restService.addRedPlayer(player)
            }
        }
        btnJoinBlue.setOnClickListener {
            if (player.team == null) {
                restService.addBluePlayer(player)
            }
        }
        btnViewRed.setOnClickListener {
            val viewPlayersFragment = ViewPlayersFragment.newInstance(game?.redTeam)
            viewPlayersFragment.show(supportFragmentManager, "TAG")
        }
        btnViewBlue.setOnClickListener {
            val viewPlayersFragment = ViewPlayersFragment.newInstance(game?.blueTeam)
            viewPlayersFragment.show(supportFragmentManager, "TAG")
        }
    }

    override fun createGameSuccess() {
    }

    override fun addRedPlayerSuccess() {
        restService.getGame()
        cvRed.setCardBackgroundColor(ContextCompat.getColor(this, R.color.redTeam))
        btnJoinRed.text = getString(R.string.joined_red)
        player.team = "RED"
    }

    override fun addBluePlayerSuccess() {
        restService.getGame()
        cvBlue.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blueTeam))
        btnJoinBlue.text = getString(R.string.joined_blue)
        player.team = "BLUE"
    }

    override fun connectComplete() {
        mqttService.subscribe(MqttTopic.GAME)
    }

    override fun gameMessageArrived(message: GameMessage) {
        if (message.type == GameMessage.Type.START && player.team != null) {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        when {
            game == null -> super.onBackPressed()
            player.isAdmin -> showDeleteGameAlert()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_join_game, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                restService.getGame()
                toast("Fetching game info")
            }
        }
        return true
    }

    private fun showDeleteGameAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Exit game")
        builder.setMessage("If you exit, the game will be deleted. Are you sure?")
        builder.setPositiveButton("Yes") { _, _ ->
            super.onBackPressed()
            restService.deleteGame()
        }
        builder.setNeutralButton("Cancel") { _, _ ->
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}