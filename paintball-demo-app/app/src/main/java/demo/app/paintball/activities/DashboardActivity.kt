package demo.app.paintball.activities

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
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
import demo.app.paintball.util.services.position_calc.PositionCalculator
import kotlinx.android.synthetic.main.activity_dashboard.*
import org.apache.commons.math3.linear.MatrixUtils
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
            calcPos()
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

    private fun calcPos() {
        val q_prev = MatrixUtils.createRealMatrix(3, 1)
        val zk = MatrixUtils.createRealMatrix(15, 1)
        val qa = MatrixUtils.createRealMatrix(15, 6)

        val zt = 1500.0
        val Q = 1000.0
        val R = 5000.0

        q_prev.setEntry(0, 0, 3743.0)
        q_prev.setEntry(1, 0, 26950.0)
        q_prev.setEntry(2, 0, 1500.0)

        zk.setEntry(0, 0, 13769.0)
        zk.setEntry(1, 0, -10971.0)
        zk.setEntry(2, 0, -19915.0)
        zk.setEntry(3, 0, 16646.0)
        zk.setEntry(4, 0, 7702.0)
        zk.setEntry(5, 0, 27617.0)
        zk.setEntry(6, 0, 8434.0)
        zk.setEntry(7, 0, -509.0)
        zk.setEntry(8, 0, 19405.0)
        zk.setEntry(9, 0, -8212.0)
        zk.setEntry(10, 0, 7767.0)
        zk.setEntry(11, 0, -6001.0)
        zk.setEntry(12, 0, 23100.0)
        zk.setEntry(13, 0, -18197.0)
        zk.setEntry(14, 0, -6753.0)


        val a1: DoubleArray = doubleArrayOf(2000.0, 5535.0, 1500.0)
        val a2: DoubleArray = doubleArrayOf(4500.0, 5515.0, 1500.0)
        val a3: DoubleArray = doubleArrayOf(6298.0, 5515.0, 1500.0)
        val a4: DoubleArray = doubleArrayOf(6272.0, 10.0, 1500.0)
        val a5: DoubleArray = doubleArrayOf(3800.0, 10.0, 1500.0)
        val a6: DoubleArray = doubleArrayOf(2000.0, 10.0, 1500.0)

        val anchors: Array<DoubleArray> = arrayOf(a1, a2, a3, a4, a5, a6)

        var k = 0
        for (i in 1..5) {
            for (j in 0..i - 1) {
                qa.setEntry(k, 0, anchors.get(i).get(0))
                qa.setEntry(k, 1, anchors.get(i).get(1))
                qa.setEntry(k, 2, anchors.get(i).get(2))
                qa.setEntry(k, 3, anchors.get(j).get(0))
                qa.setEntry(k, 4, anchors.get(j).get(1))
                qa.setEntry(k, 5, anchors.get(j).get(2))
                val anchI = anchors.get(i)
                val anchJ = anchors.get(j)
                println(
                    "pairs: ${anchI.get(0)}, ${anchI.get(1)}, ${anchI.get(2)} == ${anchJ.get(0)}, ${
                        anchJ.get(
                            1
                        )
                    }, ${anchJ.get(2)}, "
                )
                k++
            }
        }

        val start = SystemClock.uptimeMillis()
        val result = PositionCalculator.kalmanCalculation(q_prev, zk, qa, zt, Q, R)
        println(
            "Result: ${result.getEntry(0, 0)}, ${result.getEntry(1, 0)}, " +
                    "time: ${SystemClock.uptimeMillis() - start} ms"
        )

    }
}