package demo.app.paintball.util.services

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.util.*

class BluetoothService(context: Context) : BleManager(context), ConnectionObserver {

    companion object {
        val GATT_RANGING_SERVICE_UUID = UUID.fromString("f45a1000-00a6-413e-87db-580f0cab9adc")
        val GATT_RANGING_SERVICE_INFO_UUID = UUID.fromString("f45a1001-00a6-413e-87db-580f0cab9adc")
        val GATT_RANGING_SERVICE_RANGING_UUID = UUID.fromString("f45a1002-00a6-413e-87db-580f0cab9adc")
        val GATT_RANGING_SERVICE_MODE_UUID = UUID.fromString("f45a1003-00a6-413e-87db-580f0cab9adc")

        const val BLE_RANGING_SERVICE_MODE_POWERDOWN: Byte = 0x00
        const val BLE_RANGING_SERVICE_MODE_TAG_RANGING: Byte = 0x01
        const val BLE_RANGING_SERVICE_MODE_ANCHOR_RANGING: Byte = 0x02
    }

    init {
        setConnectionObserver(this)
    }

    override fun getGattCallback(): BleManagerGattCallback {
        TODO("Not yet implemented")
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        TODO("Not yet implemented")
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        TODO("Not yet implemented")
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        TODO("Not yet implemented")
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        TODO("Not yet implemented")
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        TODO("Not yet implemented")
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        TODO("Not yet implemented")
    }

    private class GattCallback : BleManagerGattCallback() {

        var mRangingService: BluetoothGattService? = null
        var mInfoCharacteristic: BluetoothGattCharacteristic? = null
        var mRangingCharacteristic: BluetoothGattCharacteristic? = null
        var mModeCharacteristic: BluetoothGattCharacteristic? = null


        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            mRangingService = gatt.getService(GATT_RANGING_SERVICE_UUID) ?: return false
            mInfoCharacteristic = mRangingService?.getCharacteristic(GATT_RANGING_SERVICE_INFO_UUID) ?: return false
            mRangingCharacteristic = mRangingService?.getCharacteristic(GATT_RANGING_SERVICE_RANGING_UUID) ?: return false
            mModeCharacteristic = mRangingService?.getCharacteristic(GATT_RANGING_SERVICE_MODE_UUID) ?: return false
            return true
        }

        override fun initialize() {
            super.initialize()
        }

        override fun onDeviceDisconnected() {
            TODO("Not yet implemented")
        }

    }
}