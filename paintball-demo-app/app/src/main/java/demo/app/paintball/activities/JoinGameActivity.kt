package demo.app.paintball.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import demo.app.paintball.R
import demo.app.paintball.data.model.Game
import demo.app.paintball.data.model.Player
import demo.app.paintball.data.mqtt.MqttHelper
import demo.app.paintball.data.rest.GameManager
import demo.app.paintball.data.rest.GameManagerImpl
import demo.app.paintball.fragments.ViewPlayersFragment
import demo.app.paintball.util.ErrorHandler
import demo.app.paintball.util.toast
import kotlinx.android.synthetic.main.activity_join_game.*
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import retrofit2.Response


class JoinGameActivity : AppCompatActivity(), GameManager.SuccessListener {

    private lateinit var gameManager: GameManager

    private var game: Game? = null

    private lateinit var player: Player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_game)

        gameManager = GameManagerImpl(
            listener = this,
            errorListener = ErrorHandler
        )
        gameManager.getGame()
        initPlayer()
        setUpStartGameButton()
        setUpTeamButtons()
    }

    private fun initPlayer() {
        player = Player().apply {
            name = intent.getStringExtra("PLAYER_NAME")!!
            isAdmin = intent.getBooleanExtra("IS_ADMIN", false)
            deviceName = "player1"
        }
    }

    private fun setUpStartGameButton() {
        if (!player.isAdmin) {
            btnStartGame.isEnabled = false
            btnStartGame.text = getString(R.string.waiting_for_admin)
        } else {
            btnStartGame.setOnClickListener {
                val mqttHelper = MqttHelper(applicationContext)
                mqttHelper.setCallback(object : MqttCallbackExtended {
                    override fun connectComplete(b: Boolean, s: String) {
                        toast("Connected to MQTT broker")
                    }

                    override fun connectionLost(throwable: Throwable) {}
                    override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                        toast("MQTT message arrived: $mqttMessage")
                    }

                    override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {}
                })
            }
        }
    }

    private fun setUpTeamButtons() {
        btnJoinRed.setOnClickListener {
            gameManager.addRedPlayer(player)
        }
        btnJoinBlue.setOnClickListener {
            gameManager.addBluePlayer(player)
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
        gameManager.getGame()
        cvRed.setCardBackgroundColor(ContextCompat.getColor(this, R.color.redTeam))
        btnJoinRed.text = getString(R.string.joined_red)
    }

    override fun addBluePlayerSuccess() {
        gameManager.getGame()
        cvBlue.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blueTeam))
        btnJoinBlue.text = getString(R.string.joined_blue)
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
                gameManager.getGame()
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
            gameManager.deleteGame()
        }
        builder.setNeutralButton("Cancel") { _, _ ->
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}