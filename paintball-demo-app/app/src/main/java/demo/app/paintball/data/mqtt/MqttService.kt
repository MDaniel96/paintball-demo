package demo.app.paintball.data.mqtt

import org.eclipse.paho.client.mqttv3.MqttMessage

interface MqttService {

    var listener: SuccessListener

    fun subscribe(topic: String)

    fun unsubscribe(topic: String)

    fun publish(topic: String, message: String)

    interface SuccessListener {
        fun connectComplete()
        fun messageArrived(topic: String, mqttMessage: MqttMessage)
    }
}