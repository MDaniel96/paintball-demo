package demo.app.paintball.util.services.ble

import android.app.Activity
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.MatrixUtils.createRealMatrix
import org.apache.commons.math3.linear.RealMatrix
import kotlin.math.pow
import kotlin.math.sqrt


class PositionCalculator(val activity: Activity) {

    // q_prev: előző pozíció                        2x1 vect  (x, y)
    // zt: tag height (given)
    // n: number of anchors   (later variable defined: ncomb=n*(n-1)/2 )

    // zk:     mérési eredmények                    ncombx1 vect (távolságkülönbségek anchorok között)
    // qa:     anchorkoordináták összepárosítva     ncombx6 matrix (ezek mondják meg hogy melyikek között zk a táv) (x, y, z)

    //  error:  megállási feltétel (iterációk közötti különbség hibahatára)     double (set to 100 or 50)
    //  maxiter:     megállási feltétel (iterációk maximális száma)             int (set to 15-30, based on runtime)


    private val q_prev = MatrixUtils.createRealMatrix(2, 1)
    private var zt = 1500.0
    private var n = 3
    private lateinit var zk: RealMatrix
    private lateinit var qa: RealMatrix
    private var error = 100.0
    private var maxiter = 30

    fun calculate(
        zk: RealMatrix,
        qa: RealMatrix
    ) {
        // g_prev
        q_prev.setEntry(0, 0, 3743.0)
        q_prev.setEntry(1, 0, 26950.0)
        this.zk = zk
        this.qa = qa
        runnable.run()
    }

    private val runnable = object : Runnable {
        override fun run() {
            val ncomb = n * (n - 1) / 2
            var q = createRealMatrix(2, 1)

            var eta = 100000.0
            var iter = 1
            val rez = createRealMatrix(ncomb, 1)
            val JAC = createRealMatrix(ncomb, 2)

            q.setEntry(0, 0, q_prev.getEntry(0, 0))
            q.setEntry(1, 0, q_prev.getEntry(1, 0))

            while (eta > error || iter < maxiter) {

                for (i in 0..ncomb - 1) {
                    val hkmValue = sqrt(
                        (q.getEntry(0, 0) - qa.getEntry(i, 0)).pow(2) +
                                (q.getEntry(1, 0) - qa.getEntry(i, 1)).pow(2) +
                                (zt - qa.getEntry(i, 2)).pow(2)
                    ) - sqrt(
                        (q.getEntry(0, 0) - qa.getEntry(i, 3)).pow(2) +
                                (q.getEntry(1, 0) - qa.getEntry(i, 4)).pow(2) +
                                (zt - qa.getEntry(i, 5)).pow(2)
                    )
                    rez.setEntry(i, 0, hkmValue - zk.getEntry(i, 0))

                    val HxValue1 = (q.getEntry(0, 0) - qa.getEntry(i, 0)) / hkmValue - (q.getEntry(
                        0,
                        0
                    ) - qa.getEntry(i, 3)) / hkmValue
                    JAC.setEntry(i, 0, HxValue1)

                    val HxValue2 = (q.getEntry(1, 0) - qa.getEntry(i, 1)) / hkmValue - (q.getEntry(
                        1,
                        0
                    ) - qa.getEntry(i, 4)) / hkmValue
                    JAC.setEntry(i, 1, HxValue2)
                }

                val JACT = JAC.transpose()
                val delta = ((MatrixUtils.inverse(JACT.multiply(JAC))).multiply(JACT)).multiply(rez)
                q = q.add(delta)

                eta = sqrt(delta.getEntry(0, 0).pow(2) + delta.getEntry(1, 0).pow(2))

                iter++

            }

            if (iter == maxiter) {
                q.setEntry(0, 0, q_prev.getEntry(0, 0))
                q.setEntry(1, 0, q_prev.getEntry(1, 0))
            }

//            activity.runOnUiThread {
            println("Result: ${q.getEntry(0, 0)}, ${q.getEntry(1, 0)}")
//            }

            q_prev.setEntry(0, 0, q.getEntry(0, 0))
            q_prev.setEntry(1, 0, q.getEntry(1, 0))

        }
    }
}