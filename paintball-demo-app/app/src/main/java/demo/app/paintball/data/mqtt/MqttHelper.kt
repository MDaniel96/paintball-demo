package demo.app.paintball.data.mqtt

import android.content.Context
import demo.app.paintball.util.toast
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*


class MqttHelper(context: Context) {

    companion object {
        const val SERVER_URL = "tcp://192.168.0.17:1883"
        const val TOPIC = "test"
    }

    var mqttAndroidClient: MqttAndroidClient

    init {
        mqttAndroidClient = MqttAndroidClient(context, SERVER_URL, MqttClient.generateClientId())
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {}
            override fun connectionLost(throwable: Throwable) {}
            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {}
            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {}
        })
        connect()
    }

    fun setCallback(callback: MqttCallbackExtended) {
        mqttAndroidClient.setCallback(callback)
    }

    private fun connect() {
        val mqttConnectOptions = MqttConnectOptions().apply {
            isAutomaticReconnect = true
            isCleanSession = false
        }

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    subscribeToTopic()
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    toast("MQTT: Failed to connect to: $SERVER_URL")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(TOPIC, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {}
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    toast("MQTT: Failed to subscribe to topic: $TOPIC")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}