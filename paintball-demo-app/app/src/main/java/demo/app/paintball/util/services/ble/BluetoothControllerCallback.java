package demo.app.paintball.util.services.ble;


import demo.app.paintball.util.services.ble.data.SensorTagRangingData;

public interface BluetoothControllerCallback {
    void onBLEConnected(BluetoothController connection);

    void onBLEDataReceived(BluetoothController connection, SensorTagRangingData data);

    void onBLEDisconnected(BluetoothController connection);

}
