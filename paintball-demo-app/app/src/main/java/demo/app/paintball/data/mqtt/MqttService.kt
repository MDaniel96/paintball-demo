package demo.app.paintball.data.mqtt

import org.eclipse.paho.client.mqttv3.MqttMessage

interface MqttService {

    var listener: SuccessListener

    fun subscribe(topic: Topic)

    fun unsubscribe(topic: Topic)

    fun publish(topic: Topic, message: String)

    interface SuccessListener {
        fun connectComplete()
        fun messageArrived(topic: Topic, mqttMessage: MqttMessage)
    }
}