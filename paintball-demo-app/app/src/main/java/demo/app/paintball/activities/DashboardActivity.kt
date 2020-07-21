package demo.app.paintball.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import demo.app.paintball.R
import demo.app.paintball.data.game.GameManager
import demo.app.paintball.data.game.rest.GameManagerImpl
import demo.app.paintball.data.model.Game
import demo.app.paintball.fragments.CreateGameFragment
import demo.app.paintball.fragments.JoinGameFragment
import demo.app.paintball.util.ErrorHandler
import kotlinx.android.synthetic.main.activity_dashboard.*
import retrofit2.Response

class DashboardActivity : AppCompatActivity(), JoinGameFragment.JoinGameListener,
    CreateGameFragment.CreateGameListener,
    GameManager.SuccessListener {

    private lateinit var gameManager: GameManager

    private lateinit var playerName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        btnJoinGame.setOnClickListener {
            val playerNameFragment = JoinGameFragment()
            playerNameFragment.show(supportFragmentManager, "TAG")
        }

        btnCreateGame.setOnClickListener {
            val createGameFragment = CreateGameFragment()
            createGameFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.TitleDialog);
            createGameFragment.show(supportFragmentManager, "TAG")
        }
    }

    override fun onResume() {
        super.onResume()

        gameManager = GameManagerImpl(
            listener = this,
            errorListener = ErrorHandler
        )
        gameManager.getGame()
    }

    override fun onJoinGame(playerName: String) {
        val intent = Intent(this, JoinGameActivity::class.java).apply {
            putExtra("PLAYER_NAME", playerName)
            putExtra("IS_ADMIN", false)
        }
        startActivity(intent)
    }

    override fun onCreateGame(playerName: String, game: Game) {
        this.playerName = playerName
        gameManager.createGame(game)
    }

    override fun getGameSuccess(response: Response<Game>) {
        if (response.code() != 404) {
            btnCreateGame.isEnabled = false
        }
    }

    override fun createGameSuccess() {
        val intent = Intent(this, JoinGameActivity::class.java).apply {
            putExtra("PLAYER_NAME", playerName)
            putExtra("IS_ADMIN", true)
        }
        startActivity(intent)
    }
}