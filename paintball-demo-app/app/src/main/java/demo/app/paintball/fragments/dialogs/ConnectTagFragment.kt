package demo.app.paintball.fragments.dialogs

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import demo.app.paintball.PaintballApplication
import demo.app.paintball.R
import kotlinx.android.synthetic.main.fragment_connect_tag.*
import java.util.*
import kotlin.collections.HashSet
import kotlin.concurrent.timerTask

class ConnectTagFragment : DialogFragment() {

    companion object {
        const val BLE_SCAN_PERIOD = 500L
    }

    private lateinit var listener: ConnectTagListener
    private lateinit var listAdapter: ArrayAdapter<String>
    private lateinit var bleScanner: BluetoothLeScanner

    private var foundTags = HashSet<BluetoothDevice>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setStyle(STYLE_NORMAL, R.style.TitleDialog)

        try {
            listener = activity as ConnectTagListener
        } catch (e: ClassCastException) {
            throw RuntimeException(e)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_connect_tag, container, false)
        dialog?.setTitle(R.string.connect_your_tag)

        listAdapter = ArrayAdapter(
            PaintballApplication.context,
            R.layout.list_item_ble_devices,
            R.id.tvDeviceName,
            foundTags.map { it.name }
        )
        val lsAvailableDevices = view.findViewById<ListView>(R.id.lsAvailableDevices)
        lsAvailableDevices.adapter = listAdapter
        lsAvailableDevices.setOnItemClickListener { adapterView, v, position, id ->
            // TODO: connect to tag
            foundTags.elementAt(position).name
            listener.onTagConnected()
            this.dismiss()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scanTags()
    }

    private fun scanTags() {
        val bleManager = (listener as Activity).getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleScanner = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleManager.adapter.bluetoothLeScanner
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP")
        }
        bleScanner.startScan(scanning)
        progressBar.visibility = View.VISIBLE
        val timer = Timer()
        timer.schedule(timerTask {
            (listener as Activity).runOnUiThread {
                if (foundTags.isNotEmpty()) {
                    stopScan()
                    timer.cancel()
                }
            }
        }, 0L, BLE_SCAN_PERIOD)
    }

    private fun stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleScanner.stopScan(scanning)
        }
        if (foundTags.isNotEmpty()) {
            listAdapter.addAll(foundTags.map { it.name })
        } else {
            tvNoTagsFound.visibility = View.VISIBLE
        }
        progressBar.visibility = View.GONE
    }

    private val scanning: ScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.name?.let {
                foundTags.add(result.device)
            }
        }
    }

    interface ConnectTagListener {
        fun onTagConnected()
    }
}