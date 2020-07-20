package demo.app.paintball.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import demo.app.paintball.R
import demo.app.paintball.data.game.GameManager
import demo.app.paintball.data.game.GameManagerListener
import demo.app.paintball.data.game.rest.GameManagerImpl
import demo.app.paintball.data.model.Game
import demo.app.paintball.data.model.Player
import demo.app.paintball.util.toast
import kotlinx.android.synthetic.main.activity_join_game.*
import retrofit2.Response

class JoinGameActivity : AppCompatActivity(), GameManagerListener {

    private lateinit var gameManager: GameManager

    private var game: Game? = null

    private lateinit var player: Player

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_game)

        gameManager = GameManagerImpl(
            listener = this
        )
        gameManager.getGame()
        initPlayer()
    }

    private fun initPlayer() {
        player = Player().apply {
            name = intent.getStringExtra("PLAYER_NAME")!!
            isAdmin = intent.getBooleanExtra("IS_ADMIN", false)
            deviceName = "player1"
        }
        if (!player.isAdmin) {
            btnStartGame.isEnabled = false
            btnStartGame.text = "Waiting for admin"
        }
    }

    private fun initHeader() {
        game?.let {
            tvGameName.text = it.name
            tvGameType.text = it.type
            tvGameAdmin.text = "Admin: ${it.admin}"
            tvGamePlayerCnt.text = "${it.playerCnt} players"
        }
    }

    override fun getGameSuccess(response: Response<Game>) {
        if (response.code() == 404) {
            toast("No game found")
        } else {
            game = response.body()
            initHeader()
        }
    }

    override fun getGameFailure(e: Throwable) {
        toast("Error: $e")
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
}