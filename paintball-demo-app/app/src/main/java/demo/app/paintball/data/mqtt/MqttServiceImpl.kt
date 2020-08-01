package demo.app.paintball.data.mqtt

import demo.app.paintball.PaintballApplication
import demo.app.paintball.R
import demo.app.paintball.util.toast
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MqttServiceImpl @Inject constructor() : MqttService {

    override lateinit var listener: MqttService.SuccessListener

    var mqttAndroidClient: MqttAndroidClient = MqttAndroidClient(
        PaintballApplication.context,
        PaintballApplication.context.getString(R.string.mqttBroker),
        MqttClient.generateClientId()
    )

    init {
        setCallback()
        connect()
    }

    private fun setCallback() {
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(b: Boolean, s: String) {
                listener.connectComplete()
            }

            override fun messageArrived(topic: String, mqttMessage: MqttMessage) {
                listener.messageArrived(topic, mqttMessage)
            }

            override fun connectionLost(throwable: Throwable) {}
            override fun deliveryComplete(iMqttDeliveryToken: IMqttDeliveryToken) {}
        })
    }

    private fun connect() {
        val mqttConnectOptions = MqttConnectOptions().apply {
            isAutomaticReconnect = true
            isCleanSession = false
        }

        mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                toast("Connected to MQTT broker")
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                toast("MQTT: Failed to connect to: ${PaintballApplication.context.getString(R.string.mqttBroker)}")
            }
        })
    }

    override fun subscribe(topic: String) {
        mqttAndroidClient.subscribe(topic, 0, null)
    }

    override fun unsubscribe(topic: String) {
        mqttAndroidClient.unsubscribe(topic)
    }

    override fun publish(topic: String, message: String) {
        mqttAndroidClient.publish(topic, message.toByteArray(), 0, false)
    }
}