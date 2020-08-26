package demo.app.paintball.util.services.position_calc

import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.MatrixUtils.createRealIdentityMatrix
import org.apache.commons.math3.linear.MatrixUtils.createRealMatrix
import org.apache.commons.math3.linear.RealMatrix
import kotlin.math.pow
import kotlin.math.sqrt


class PositionCalculator {

    // q_prev: előző pozíció                        1x3 vect  (x, y, z)
    // zk:     mérési eredmények                    1x28 vect (távolságkülönbségek anchorok között)
    // qa:     anchorkoordináták összepárosítva     6x28 vect (ezek mondják meg hogy melyikek között zk a táv) (x, y, z)
    //  Q:     const                                int
    //  R:     const                                int

    // mm minden

    // xkm      1x2
    // pkm      2x2
    // Rk       28x28
    // hkm      28x1
    // Hx       28x2

    companion object {
        fun kalmanCalculation(
            q_prev: RealMatrix,
            zk: RealMatrix,
            qa: RealMatrix,
            zt: Double,
            Q: Double,
            R: Double
        ): RealMatrix {

            val xkm = createRealMatrix(2, 1)
            xkm.setEntry(0, 0, q_prev.getEntry(0, 0))
            xkm.setEntry(1, 0, q_prev.getEntry(1, 0))

            val Pkm = createRealIdentityMatrix(2).scalarMultiply(Q)
            val Rk = createRealIdentityMatrix(15).scalarMultiply(R)

            val hkm = createRealMatrix(15, 1)
            val Hx = createRealMatrix(15, 2)

            for (i in 0..15 - 1) {
                val hkmValue = sqrt(
                    (xkm.getEntry(0, 0) - qa.getEntry(i, 0)).pow(2) +
                            (xkm.getEntry(1, 0) - qa.getEntry(i, 1)).pow(2) +
                            (zt - qa.getEntry(i, 2)).pow(2)
                ) - sqrt(
                    (xkm.getEntry(0, 0) - qa.getEntry(i, 3)).pow(2) +
                            (xkm.getEntry(1, 0) - qa.getEntry(i, 4)).pow(2) +
                            (zt - qa.getEntry(i, 5)).pow(2)
                )
                hkm.setEntry(i, 0, hkmValue)

                val HxValue1 = (xkm.getEntry(0, 0) - qa.getEntry(i, 0)) / hkmValue - (xkm.getEntry(
                    0,
                    0
                ) - qa.getEntry(i, 3)) / hkmValue
                Hx.setEntry(i, 0, HxValue1)

                val HxValue2 = (xkm.getEntry(1, 0) - qa.getEntry(i, 1)) / hkmValue - (xkm.getEntry(
                    1,
                    0
                ) - qa.getEntry(i, 4)) / hkmValue
                Hx.setEntry(i, 1, HxValue2)
            }

            val Hxt = Hx.transpose()
            val vk = zk.subtract(hkm)
            val Sk = ((Hx.multiply(Pkm)).multiply(Hxt)).add(Rk)
            val Kk = (Pkm.multiply(Hxt)).multiply(MatrixUtils.inverse(Sk))

            val q = (Kk.multiply(vk)).add(xkm)
            return q
        }
    }
}