package demo.app.paintball.util.services.ble

import android.app.Activity
import android.os.SystemClock
import demo.app.paintball.util.toast
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.MatrixUtils.createRealMatrix
import org.apache.commons.math3.linear.RealMatrix
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt


class PositionCalculator(val activity: Activity) {

    // q_prev: előző pozíció                        2x1 vect  (x, y)
    // zt: tag height (given)
    // n: number of anchors   (later variable defined: ncomb=n*(n-1)/2 )

    // zk:     mérési eredmények                    ncombx1 vect (távolságkülönbségek anchorok között)
    // qa:     anchorkoordináták összepárosítva     ncombx6 matrix (ezek mondják meg hogy melyikek között zk a táv) (x, y, z)

    //  error:  megállási feltétel (iterációk közötti különbség hibahatára)     double (set to 100 or 50)
    //  maxiter:     megállási feltétel (iterációk maximális száma)             int (set to 15-30, based on runtime)


    private var zt = 1100.0
    private var n = 8
    private lateinit var zk: RealMatrix
    private lateinit var qa: RealMatrix
    private lateinit var q_prev: RealMatrix
    private var error = 100.0
    private var maxiter = 30

    fun calculate(
        zk: RealMatrix,
        qa: RealMatrix,
        q_prev: RealMatrix
    ) {

        this.zk = zk
        this.qa = qa
        this.q_prev = q_prev
        runnable.run()
    }

    private val runnable = object : Runnable {
        override fun run() {

            val start = SystemClock.uptimeMillis()

            var ncomb = n * (n - 1) / 2 - 1
            var q = createRealMatrix(2, 1)

            var eta = 100000.0
            var iter = 1

            q.setEntry(0, 0, q_prev.getEntry(0, 0))
            q.setEntry(1, 0, q_prev.getEntry(1, 0))

            while ((zk.getEntry(ncomb, 0) == -32768.0 || zk.getEntry(ncomb, 0) == 0.0) && ncomb > 0) {
                ncomb = ncomb - 1
            }

            if (ncomb + 1 < 3) {
                iter = maxiter
            }

            while (eta > error && iter < maxiter) {

                val rez = createRealMatrix(ncomb + 1, 1)
                val JAC = createRealMatrix(ncomb + 1, 2)
                var j = 0

                for (i in 0..ncomb) {
                    if (zk.getEntry(i, 0) == -32768.0 || zk.getEntry(i, 0) == -1.0) {
                        continue
                    }

                    val hkmValue1 = sqrt(
                        (q.getEntry(0, 0) - qa.getEntry(i, 0)).pow(2) +
                                (q.getEntry(1, 0) - qa.getEntry(i, 1)).pow(2) +
                                (zt - qa.getEntry(i, 2)).pow(2)
                    )
                    val hkmValue2 = sqrt(
                        (q.getEntry(0, 0) - qa.getEntry(i, 3)).pow(2) +
                                (q.getEntry(1, 0) - qa.getEntry(i, 4)).pow(2) +
                                (zt - qa.getEntry(i, 5)).pow(2)
                    )
                    rez.setEntry(j, 0, hkmValue1 - hkmValue2 - zk.getEntry(i, 0))

                    val HxValue1 =
                        (q.getEntry(0, 0) - qa.getEntry(i, 0)) / hkmValue1 - (q.getEntry(
                            0,
                            0
                        ) - qa.getEntry(i, 3)) / hkmValue2
                    JAC.setEntry(j, 0, HxValue1)

                    val HxValue2 =
                        (q.getEntry(1, 0) - qa.getEntry(i, 1)) / hkmValue1 - (q.getEntry(
                            1,
                            0
                        ) - qa.getEntry(i, 4)) / hkmValue2
                    JAC.setEntry(j, 1, HxValue2)
                    j++
                }

                if (j < 3) {
                    break
                }

                val JACT = JAC.transpose()
                val a = JACT.multiply(JAC)
                val b = MatrixUtils.inverse(a)
                val c = b.multiply(JACT)
                val delta = c.multiply(rez)
                q.setEntry(0, 0, q.getEntry(0, 0) - delta.getEntry(0, 0))
                q.setEntry(1, 0, q.getEntry(1, 0) - delta.getEntry(1, 0))

                eta = sqrt(delta.getEntry(0, 0).pow(2) + delta.getEntry(1, 0).pow(2))

                iter++

            }

            if (iter == maxiter) {
                q.setEntry(0, 0, q_prev.getEntry(0, 0))
                q.setEntry(1, 0, q_prev.getEntry(1, 0))
            }

//            activity.runOnUiThread {
            println(
                "Result: ${q.getEntry(0, 0)}, ${q.getEntry(1, 0)}, time: ${SystemClock.uptimeMillis() - start} ms"
            )
//            }
            toast("${round(q.getEntry(0, 0)) / 1000} | ${round(q.getEntry(1, 0)) / 1000}")

            q_prev.setEntry(0, 0, q.getEntry(0, 0))
            q_prev.setEntry(1, 0, q.getEntry(1, 0))

        }
    }
}