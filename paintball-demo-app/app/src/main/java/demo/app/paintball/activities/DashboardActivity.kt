package demo.app.paintball.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import demo.app.paintball.R
import demo.app.paintball.fragments.PlayerNameFragment
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardActivity : AppCompatActivity(), PlayerNameFragment.PlayerNameSentListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        btnJoinGame.setOnClickListener {
            val playerNameFragment = PlayerNameFragment()
            playerNameFragment.show(supportFragmentManager, "TAG")
        }
    }

    override fun onNameSent(name: String) {
        val intent = Intent(this, JoinGameActivity::class.java).apply {
            putExtra("PLAYER_NAME", name)
        }
        startActivity(intent)
    }
}