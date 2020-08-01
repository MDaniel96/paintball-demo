package demo.app.paintball.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import demo.app.paintball.PaintballApplication
import demo.app.paintball.R
import demo.app.paintball.data.model.Game
import demo.app.paintball.data.model.Player
import demo.app.paintball.data.mqtt.MqttService
import demo.app.paintball.data.rest.RestService
import demo.app.paintball.fragments.ViewPlayersFragment
import demo.app.paintball.util.ErrorHandler
import demo.app.paintball.util.services.PlayerService
import demo.app.paintball.util.toast
import kotlinx.android.synthetic.main.activity_join_game.*
import org.eclipse.paho.client.mqttv3.MqttMessage
import retrofit2.Response
import javax.inject.Inject

class JoinGameActivity : AppCompatActivity(), RestService.SuccessListener,
    MqttService.SuccessListener {

    @Inject
    lateinit var restService: RestService

    @Inject
    lateinit var mqttService: MqttService

    @Inject
    lateinit var playerService: PlayerService

    private var game: Game? = null

    private lateinit var player: Player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_game)

        playerService = PaintballApplication.services.player()
        restService = PaintballApplication.services.rest().apply {
            listener = this@JoinGameActivity
            errorListener = ErrorHandler
        }
        mqttService = PaintballApplication.services.mqtt().apply {
            listener = this@JoinGameActivity
        }

        restService.getGame()
        player = playerService.player
        setUpStartGameButton()
        setUpTeamButtons()
    }

    private fun setUpStartGameButton() {
        if (!player.isAdmin) {
            btnStartGame.isEnabled = false
            btnStartGame.text = getString(R.string.waiting_for_admin)
        } else {
            btnStartGame.setOnClickListener {
                mqttService.publish("game", "start")
            }
        }
    }

    private fun setUpTeamButtons() {
        btnJoinRed.setOnClickListener {
            restService.addRedPlayer(player)
        }
        btnJoinBlue.setOnClickListener {
            restService.addBluePlayer(player)
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

    private fun initTexts() {
        game?.let {
            tvGameName.text = it.name
            tvGameType.text = it.type
            tvGameAdmin.text =
                String.format(resources.getString(R.string.admin_is), it.admin)
            tvGamePlayerCnt.text =
                String.format(resources.getString(R.string.player_cnt), it.playerCnt)
            btnViewRed.text =
                String.format(resources.getString(R.string.view_players_), it.redTeam.size)
            btnViewBlue.text =
                String.format(resources.getString(R.string.view_players_), it.blueTeam.size)
        }
    }

    override fun getGameSuccess(response: Response<Game>) {
        if (response.code() == 404) {
            toast("No game found")
        } else {
            game = response.body()
            initTexts()
        }
    }

    override fun createGameSuccess() {
    }

    override fun addRedPlayerSuccess() {
        restService.getGame()
        cvRed.setCardBackgroundColor(ContextCompat.getColor(this, R.color.redTeam))
        btnJoinRed.text = getString(R.string.joined_red)
        playerService.player.team = "RED"
    }

    override fun addBluePlayerSuccess() {
        restService.getGame()
        cvBlue.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blueTeam))
        btnJoinBlue.text = getString(R.string.joined_blue)
        playerService.player.team = "BLUE"
    }

    override fun connectComplete() {
        mqttService.subscribe("game")
    }

    override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
        if (topic == "game" && mqttMessage.toString() == "start") {
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