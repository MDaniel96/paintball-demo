package demo.app.paintball.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import demo.app.paintball.R
import demo.app.paintball.data.game.GameManagerListener
import demo.app.paintball.data.game.rest.GameManagerImpl
import demo.app.paintball.data.model.Game
import demo.app.paintball.fragments.JoinGameFragment
import demo.app.paintball.util.toast
import kotlinx.android.synthetic.main.activity_dashboard.*
import retrofit2.Response

class DashboardActivity : AppCompatActivity(), JoinGameFragment.JoinGameListener,
    GameManagerListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        btnJoinGame.setOnClickListener {
            val playerNameFragment = JoinGameFragment()
            playerNameFragment.show(supportFragmentManager, "TAG")
        }
    }

    override fun onResume() {
        super.onResume()

        val gameManager = GameManagerImpl(
            listener = this
        )
        gameManager.getGame()
    }

    override fun onJoinGame(name: String) {
        val intent = Intent(this, JoinGameActivity::class.java).apply {
            putExtra("PLAYER_NAME", name)
            putExtra("IS_ADMIN", false)
        }
        startActivity(intent)
    }

    override fun getGameSuccess(response: Response<Game>) {
        if (response.code() != 404) {
            btnCreateGame.isEnabled = false
        }
    }

    override fun getGameFailure(e: Throwable) {
        toast("Error: $e")
    }
}