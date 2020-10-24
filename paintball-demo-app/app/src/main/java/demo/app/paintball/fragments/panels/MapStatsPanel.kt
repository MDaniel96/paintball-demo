package demo.app.paintball.fragments.panels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import demo.app.paintball.R

class MapStatsPanel : Fragment() {

    private lateinit var statsLayout: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statsLayout = view.findViewById<LinearLayout>(R.id.statsLayout)
    }

    fun show() {
        statsLayout.animate().translationX(0F)
    }

    fun hide() {
        statsLayout.animate().translationX(-300F)
    }
}