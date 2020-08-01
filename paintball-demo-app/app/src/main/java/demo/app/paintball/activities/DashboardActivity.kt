package demo.app.paintball.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import demo.app.paintball.PaintballApplication
import demo.app.paintball.R
import demo.app.paintball.data.model.Game
import demo.app.paintball.data.model.Player
import demo.app.paintball.data.rest.RestService
import demo.app.paintball.fragments.CreateGameFragment
import demo.app.paintball.fragments.JoinGameFragment
import demo.app.paintball.util.ErrorHandler
import demo.app.paintball.util.services.PlayerService
import kotlinx.android.synthetic.main.activity_dashboard.*
import retrofit2.Response
import javax.inject.Inject

class DashboardActivity : AppCompatActivity(), JoinGameFragment.JoinGameListener,
    CreateGameFragment.CreateGameListener,
    RestService.SuccessListener {

    @Inject
    lateinit var restService: RestService

    @Inject
    lateinit var playerService: PlayerService

    private lateinit var playerName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        playerService = PaintballApplication.services.player()
        btnJoinGame.setOnClickListener {
            val playerNameFragment = JoinGameFragment()
            playerNameFragment.show(supportFragmentManager, "TAG")
        }
        btnCreateGame.setOnClickListener {
            val createGameFragment = CreateGameFragment()
            createGameFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.TitleDialog)
            createGameFragment.show(supportFragmentManager, "TAG")
        }
    }

    override fun onResume() {
        super.onResume()

        restService = PaintballApplication.services.rest().apply {
            listener = this@DashboardActivity
            errorListener = ErrorHandler
        }
        restService.getGame()
    }

    override fun onJoinGame(playerName: String) {
        playerService.player = Player(name = playerName, isAdmin = false)
        val intent = Intent(this, JoinGameActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateGame(playerName: String, game: Game) {
        this.playerName = playerName
        restService.createGame(game)
    }

    override fun getGameSuccess(response: Response<Game>) {
        if (response.code() != 404) {
            btnCreateGame.isEnabled = false
        }
    }

    override fun createGameSuccess() {
        playerService.player = Player(name = playerName, isAdmin = true)
        val intent = Intent(this, JoinGameActivity::class.java)
        startActivity(intent)
    }

    override fun addRedPlayerSuccess() {
    }

    override fun addBluePlayerSuccess() {
    }
}