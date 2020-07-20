package demo.app.paintball.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import demo.app.paintball.R
import kotlinx.android.synthetic.main.fragment_player_name.*

class JoinGameFragment : DialogFragment() {

    private lateinit var listener: JoinGameListener

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = if (targetFragment != null) {
                targetFragment as JoinGameListener
            } else {
                activity as JoinGameListener
            }
        } catch (e: ClassCastException) {
            throw RuntimeException(e)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player_name, container, false)
        dialog?.setTitle(R.string.itemPlayerName)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnDone.setOnClickListener{
            val name = etPlayerName.text.toString()
            if (name != "") {
                listener.onJoinGame(etPlayerName.text.toString())
                dismiss()
            } else {
                etPlayerName.error = "Please write something"
            }
        }
    }

    interface JoinGameListener {
        fun onJoinGame(name: String)
    }
}