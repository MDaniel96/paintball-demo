package demo.app.paintball.util.services.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import demo.app.paintball.util.services.ble.data.SensorTagRangingData;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.observer.ConnectionObserver;

public class BluetoothController extends BleManager implements ConnectionObserver {
    private final static String TAG = BluetoothController.class.getSimpleName();

    public final static UUID GATT_RANGING_SERVICE_UUID = UUID.fromString("f45a1000-00a6-413e-87db-580f0cab9adc");
    public final static UUID GATT_RANGING_SERVICE_INFO_UUID = UUID.fromString("f45a1001-00a6-413e-87db-580f0cab9adc");
    public final static UUID GATT_RANGING_SERVICE_RANGING_UUID = UUID.fromString("f45a1002-00a6-413e-87db-580f0cab9adc");
    public final static UUID GATT_RANGING_SERVICE_MODE_UUID = UUID.fromString("f45a1003-00a6-413e-87db-580f0cab9adc");

    public final static UUID GATT_ACC_SERVICE_UUID = UUID.fromString("f45a2000-00a6-413e-87db-580f0cab9adc");
    public final static UUID GATT_ACC_SERVICE_CONTROL_UUID = UUID.fromString("f45a2001-00a6-413e-87db-580f0cab9adc");
    public final static UUID GATT_ACC_SERVICE_SENSOR_UUID = UUID.fromString("f45a2002-00a6-413e-87db-580f0cab9adc");

    public final static byte BLE_RANGING_SERVICE_MODE_TAG_RANGING = 0x01;
    public final static byte BLE_RANGING_SERVICE_MODE_ANCHOR_RANGING = 0x02;

    private BluetoothGattService mRangingService;
    private BluetoothGattCharacteristic mInfoCharacteristic;
    private BluetoothGattCharacteristic mRangingCharacteristic;
    private BluetoothGattCharacteristic mModeCharacteristic;

    private BluetoothGattService mAccService;
    private BluetoothGattCharacteristic mAccControlCharacteristic;
    private BluetoothGattCharacteristic mAccValuesCharacteristic;

    private List<BluetoothControllerCallback> mServiceCallbacks = new LinkedList<>();
    private UWBNetworkInfo mNetworkDescriptor = new UWBNetworkInfo();


    public BluetoothController(@NonNull Context context) {
        super(context);
        setConnectionObserver(this);
    }

    @Override
    public void log(int priority, @NonNull String message) {
        Log.d(TAG, message);
    }

    public void release() {
        disableNotifications(mRangingCharacteristic).enqueue();
        disableNotifications(mAccValuesCharacteristic).enqueue();
        disconnect().enqueue();
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MyGattCallback();
    }


    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {
    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {
    }

    @Override
    public void onDeviceFailedToConnect(@NonNull BluetoothDevice device, int reason) {
        for (BluetoothControllerCallback cb : mServiceCallbacks)
            cb.onBLEDisconnected(this);
    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {
        System.out.println("BLE: ready");
        Log.d(TAG, "Running connected callbacks");
        updateNetworkInfo();
        for (BluetoothControllerCallback cb : mServiceCallbacks)
            cb.onBLEConnected(this);
    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device, int reason) {
        Log.d(TAG, "Running disconnected callbacks");
        for (BluetoothControllerCallback cb : mServiceCallbacks)
            cb.onBLEDisconnected(this);
    }


    private class MyGattCallback extends BleManagerGattCallback {
        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            mRangingService = gatt.getService(GATT_RANGING_SERVICE_UUID);
            mAccService = gatt.getService(GATT_ACC_SERVICE_UUID);
            if (mRangingService == null || mAccService == null) {
                return false;
            }

            mInfoCharacteristic = mRangingService.getCharacteristic(GATT_RANGING_SERVICE_INFO_UUID);
            mRangingCharacteristic = mRangingService.getCharacteristic(GATT_RANGING_SERVICE_RANGING_UUID);
            mModeCharacteristic = mRangingService.getCharacteristic(GATT_RANGING_SERVICE_MODE_UUID);

            mAccControlCharacteristic = mAccService.getCharacteristic(GATT_ACC_SERVICE_CONTROL_UUID);
            mAccValuesCharacteristic = mAccService.getCharacteristic(GATT_ACC_SERVICE_SENSOR_UUID);

            System.out.println("BLE: required");

            return mInfoCharacteristic != null &&
                    mRangingCharacteristic != null &&
                    mModeCharacteristic != null &&
                    mAccControlCharacteristic != null &&
                    mAccValuesCharacteristic != null;
        }

        @Override
        protected void initialize() {
            beginAtomicRequestQueue()
                    .add(requestMtu(247)
                            .with((device, mtu) -> log(Log.INFO, "MTU set to " + mtu))
                            .fail((device, status) -> log(Log.WARN, "Requested MTU not supported: " + status)))
//                    .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
//                            .fail((device, status) -> log(Log.WARN, "Requested PHY not supported: " + status)))
                    .add(enableNotifications(mAccValuesCharacteristic))
                    .add(enableNotifications(mRangingCharacteristic))
                    .done(device -> log(Log.INFO, "Target initialized"))
                    .enqueue();

            setNotificationCallback(mRangingCharacteristic).with(new DataReceivedCallback() {
                @Override
                public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                    switch (mModeCharacteristic.getValue()[0]) {
                        case BLE_RANGING_SERVICE_MODE_TAG_RANGING: {
                            SensorTagRangingData userdata = SensorTagRangingData.parse(data.getValue(), mNetworkDescriptor.getAnchorCount());
                            for (BluetoothControllerCallback cb : mServiceCallbacks)
                                cb.onBLEDataReceived(BluetoothController.this, userdata);
                        }
                        break;
//                        case BLE_RANGING_SERVICE_MODE_ANCHOR_RANGING: {
//                            SensorAnchorRangingData userdata = SensorAnchorRangingData.parse(data.getValue(), mNetworkDescriptor.getAnchorCount());
//                            for (BluetoothControllerCallback cb : mServiceCallbacks)
//                                cb.onBLEDataReceived(BluetoothController.this, userdata);
//                        }
//                        break;
                        default:
                            break;
                    }
                }
            });

//            setNotificationCallback(mAccValuesCharacteristic).with(new DataReceivedCallback() {
//                @Override
//                public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
//                    List<SensorAccelerationData> datas = SensorTagRangingData.parse(data.getValue(), 7);
//                    for(SensorAccelerationData d : datas) {
//                        for (BluetoothControllerCallback cb : mServiceCallbacks)
//                            cb.onBLEDataReceived(BluetoothController.this, d);
//                    }
//                }
//            });
        }

        @Override
        protected void onDeviceDisconnected() {
            Log.i(TAG, "Device disconnected");

            mRangingService = null;
            mAccService = null;
        }
    }

    public UWBNetworkInfo getUWBNetworkInfo() {
        return mNetworkDescriptor;
    }

    public void setRangingMode(final byte mode) {
        writeCharacteristic(mModeCharacteristic, new byte[]{mode})
                .fail((device, status) -> Log.e(TAG, "Faile to write ranging mode: " + status))
                .enqueue();
    }

    public void setAccMode(final byte mode) {
        writeCharacteristic(mAccControlCharacteristic, new byte[]{mode})
                .fail((device, status) -> Log.e(TAG, "Failed to write mode: " + status))
                .enqueue();
    }


    public void updateNetworkInfo() {
        readCharacteristic(mInfoCharacteristic).with((device, data) -> {
            ByteBuffer bb = ByteBuffer.wrap(data.getValue());
            bb.order(ByteOrder.LITTLE_ENDIAN);

            mNetworkDescriptor.setGroupID(bb.getShort() & 0xFFFF);
            mNetworkDescriptor.setTagID(bb.getShort() & 0xFFFF);
            mNetworkDescriptor.setAnchorCount(bb.getShort());
            mNetworkDescriptor.setTagCount(bb.getShort());

            int failed_notif_count = bb.getInt();
            int notif_count = bb.getInt();

            Log.d(TAG, mNetworkDescriptor.toString() + " (" + failed_notif_count + "/" + notif_count + ")");
        }).enqueue();


    }

    public void addCallback(BluetoothControllerCallback callback) {
        this.mServiceCallbacks.add(callback);
        Log.d(TAG, "add callback, has " + mServiceCallbacks.size() + " callbacks");
    }

    public void removeCallback(BluetoothControllerCallback callback) {
        this.mServiceCallbacks.remove(callback);
        Log.d(TAG, "remove callback, has " + mServiceCallbacks.size() + " callbacks");
    }
}
