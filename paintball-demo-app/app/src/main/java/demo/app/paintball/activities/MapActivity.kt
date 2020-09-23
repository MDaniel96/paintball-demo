package demo.app.paintball.activities

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import demo.app.paintball.PaintballApplication
import demo.app.paintball.R
import demo.app.paintball.data.model.Game
import demo.app.paintball.data.mqtt.MqttService
import demo.app.paintball.data.mqtt.Topic
import demo.app.paintball.data.mqtt.messages.PositionMessage
import demo.app.paintball.data.rest.RestService
import demo.app.paintball.fragments.buttons.ChatButtonsFragment
import demo.app.paintball.fragments.buttons.MainButtonsFragment
import demo.app.paintball.map.renderables.Map
import demo.app.paintball.map.rendering.MapView
import demo.app.paintball.map.sensors.GestureSensor
import demo.app.paintball.map.sensors.Gyroscope
import demo.app.paintball.util.ErrorHandler
import demo.app.paintball.util.getTeamTopic
import demo.app.paintball.util.services.PlayerService
import demo.app.paintball.util.services.ble.BluetoothController
import demo.app.paintball.util.services.ble.BluetoothControllerCallback
import demo.app.paintball.util.services.ble.PositionCalculator
import demo.app.paintball.util.services.ble.data.SensorTagRangingData
import demo.app.paintball.util.toDegree
import demo.app.paintball.util.toast
import kotlinx.android.synthetic.main.activity_map.*
import org.apache.commons.math3.linear.MatrixUtils
import org.eclipse.paho.client.mqttv3.MqttMessage
import retrofit2.Response
import javax.inject.Inject


class MapActivity : AppCompatActivity(), GestureSensor.GestureListener, Gyroscope.GyroscopeListener,
    RestService.SuccessListener, MqttService.SuccessListener, BluetoothControllerCallback {

    companion object {
        const val SPYING_TIME = 7_000L
        const val SPYING_RECHARGE_TIME = 12_000L
    }

    @Inject
    lateinit var restService: RestService

    @Inject
    lateinit var mqttService: MqttService

    @Inject
    lateinit var playerService: PlayerService

    private var game: Game? = null
    private var isMapButtonsOpen = false

    private lateinit var map: MapView
    private lateinit var mainButtons: MainButtonsFragment
    private lateinit var chatButtons: ChatButtonsFragment

    private lateinit var gyroscope: Gyroscope

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        map = mapView
        mainButtons = supportFragmentManager.findFragmentById(R.id.mainButtonsFragment) as MainButtonsFragment
        chatButtons = supportFragmentManager.findFragmentById(R.id.chatButtonsFragment) as ChatButtonsFragment

        mainButtons.initLevel(0)
        chatButtons.initLevel(1)

        map.setOnTouchListener(GestureSensor(gestureListener = this, scrollPanel = buttonsPanel))
        gyroscope = Gyroscope(gyroscopeListener = this)

        playerService = PaintballApplication.services.player()
        restService = PaintballApplication.services.rest().apply { listener = this@MapActivity; errorListener = ErrorHandler }
        mqttService = PaintballApplication.services.mqtt().apply { listener = this@MapActivity }

        restService.getGame()

        fabActivateButtons.setOnClickListener {
            if (isMapButtonsOpen) hideButtons() else showButtons()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0x13)
        } else {
            initBluetooth()
        }
    }

    override fun onResume() {
        super.onResume()
        gyroscope.start()
    }

    override fun onPause() {
        super.onPause()
        gyroscope.stop()
    }

    override fun onBackPressed() {
        while (true) {
            map.setPlayerPosition(Map.playerPosX + 15, Map.playerPosY)
            Thread.sleep(30)
        }
    }

    override fun onScaleChanged(scaleFactor: Float) {
        map.zoom(scaleFactor)
    }

    override fun onZoomIn() {
        hideButtons()
    }

    override fun onZoomOut() {
        gameDetailLayout.animate().translationX(0F)
    }

    override fun onScrollUp() {
        mainButtons.changeLevel(-1)
        chatButtons.changeLevel(0)
    }

    override fun onScrollDown() {
        mainButtons.changeLevel(0)
        chatButtons.changeLevel(1)
    }

    override fun onOrientationChanged(radian: Float) {
        map.setPlayerOrientation(radian.toDegree())
    }

    override fun getGameSuccess(response: Response<Game>) {
        game = response.body()
        addPlayersToMap()
        mqttService.subscribe(playerService.player.getTeamTopic())
    }

    private fun addPlayersToMap() {
        game?.redTeam
            ?.filter { it.name != playerService.player.name }
            ?.forEach { map.addRedPlayer(it.name) }
        game?.blueTeam
            ?.filter { it.name != playerService.player.name }
            ?.forEach { map.addBluePlayer(it.name) }
    }

    override fun createGameSuccess() {
    }

    override fun addRedPlayerSuccess() {
    }

    override fun addBluePlayerSuccess() {
    }

    override fun connectComplete() {
    }

    override fun messageArrived(topic: Topic, mqttMessage: MqttMessage) {
        when (topic) {
            Topic.RED_TEAM, Topic.BLUE_TEAM -> {
                val message = PositionMessage.parse(mqttMessage.toString())
                map.setDotPosition(message.playerName, message.posX, message.posY)
            }
            Topic.GAME -> {
            }
        }
    }

    private fun showButtons() {
        isMapButtonsOpen = true
        fabActivateButtons.setImageDrawable(
            ResourcesCompat.getDrawable(resources, R.drawable.ic_unfold_less, null)
        )
        fabActivateButtons.animate().rotation(180F)
        gameDetailLayout.animate().translationX(0F)

        mainButtons.show()
        chatButtons.show()
    }

    private fun hideButtons() {
        isMapButtonsOpen = false
        fabActivateButtons.setImageDrawable(
            ResourcesCompat.getDrawable(resources, R.drawable.ic_unfold_more, null)
        )
        fabActivateButtons.animate().rotation(-180F)
        gameDetailLayout.animate().translationX(-300F)

        mainButtons.hide()
        chatButtons.hide()
    }


    //================================================================================
    //  BLUETOOTH-TEST
    //================================================================================


    private lateinit var foundDevice: BluetoothDevice
    private lateinit var bluetoothController: BluetoothController

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0x13) {
            initBluetooth()
        }
    }


    private val scanCallback: ScanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result?.device?.name != null) {
                toast("Found device: ${result.device.name}")
                foundDevice = result.device
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initBluetooth() {

        bluetoothController = BluetoothController(this)
        bluetoothController.addCallback(this)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapater = bluetoothManager.getAdapter()
        val scanner = bluetoothAdapater.getBluetoothLeScanner()
        scanner.startScan(scanCallback)
        val handler = Handler()
        handler.postDelayed({
            scanner.stopScan(scanCallback)
            bluetoothController.connect(foundDevice)
                .useAutoConnect(true)
                .timeout(100000)
                .retry(3, 100)
                .done { toast("Connected to device") }
                .enqueue();
        }, 3000L)
    }

    private val ancno = 8
    private val anccomb = ancno * (ancno - 1) / 2
    private var zk = MatrixUtils.createRealMatrix(anccomb, 1)
    private val qa = MatrixUtils.createRealMatrix(anccomb, 6)
    private var q_prev = MatrixUtils.createRealMatrix(2, 1)
    private var positionCalculator = PositionCalculator(this)
    private var trid = 0

    override fun onBLEConnected(connection: BluetoothController) {
        bluetoothController.setRangingMode(BluetoothController.BLE_RANGING_SERVICE_MODE_TAG_RANGING)

        // qa
        val a0: DoubleArray = doubleArrayOf(0.0, 0.0, 1100.0)
        val a1: DoubleArray = doubleArrayOf(3795.0, 0.0, 1100.0)
        val a2: DoubleArray = doubleArrayOf(-80.0, 4135.0, 1100.0)
        val a3: DoubleArray = doubleArrayOf(3795.0, 4135.0, 1100.0)
        val a4: DoubleArray = doubleArrayOf(0.0, 0.0, 0.0)
        val a5: DoubleArray = doubleArrayOf(0.0, 0.0, 0.0)
        val a6: DoubleArray = doubleArrayOf(0.0, 0.0, 0.0)
        val a7: DoubleArray = doubleArrayOf(0.0, 0.0, 0.0)

        val anchors: Array<DoubleArray> = arrayOf(a0, a1, a2, a3, a4, a5, a6, a7)
        var k = 0
        for (i in 1..(ancno - 1)) {
            for (j in 0..i - 1) {
                qa.setEntry(k, 0, anchors.get(i).get(0))
                qa.setEntry(k, 1, anchors.get(i).get(1))
                qa.setEntry(k, 2, anchors.get(i).get(2))
                qa.setEntry(k, 3, anchors.get(j).get(0))
                qa.setEntry(k, 4, anchors.get(j).get(1))
                qa.setEntry(k, 5, anchors.get(j).get(2))
                k++
            }
        }
        // q_prev
        var xacc = 0.0
        var yacc = 0.0
        var n = 0
        for (i in 0..ancno - 1) {
            if (anchors.get(i).get(2) != 0.0) {
                xacc = xacc + anchors.get(i).get(0)
                yacc = yacc + anchors.get(i).get(1)
                n++
            }
        }
        q_prev.setEntry(0, 0, xacc / n)
        q_prev.setEntry(1, 0, yacc / n)

    }

    override fun onBLEDataReceived(connection: BluetoothController, data: SensorTagRangingData?) {
        val ranges = data?.ranges
        val rangesStr = mutableListOf<String>()
        data?.ranges?.forEach {
            rangesStr.add(it.toString())
        }
        toast("BLE data recieved: ${rangesStr}")

        //==========================
        //  POSITION CALCULATION
        //==========================

        trid = data?.ranges?.get(0)!!.toInt()
        for (i in 0..anccomb - 1) {
            zk.setEntry(i, 0, data?.ranges?.get(i + 1)!!.toDouble())
        }

        println("trid: ${trid}")

        positionCalculator.calculate(zk, qa, q_prev)

    }

    override fun onBLEDisconnected(connection: BluetoothController) {
    }

    override fun onDestroy() {
        super.onDestroy()

        bluetoothController.disconnect()
    }
}