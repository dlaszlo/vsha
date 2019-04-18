package hu.dlaszlo.vsha.mqtt

import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentLinkedQueue

@Component
class Mqtt {
    private val logger = LoggerFactory.getLogger(Mqtt::class.java)

    @Value("\${mqtt.qos}")
    private var qos: Int = 0

    @Value("\${mqtt.broker}")
    private lateinit var broker: String

    @Value("\${mqtt.clientId}")
    private lateinit var clientId: String

    @Value("\${mqtt.username}")
    private lateinit var username: String

    @Value("\${mqtt.password}")
    private lateinit var password: String

    @Value("\${mqtt.keepAliveInterval}")
    private val keepAliveInterval: Int = 0

    private lateinit var client: MqttClient

    val queue = ConcurrentLinkedQueue<Message>()

    fun publish(topic: String, payload: String, retained: Boolean) {
        client.publish(topic, payload.toByteArray(), qos, retained)
    }

    fun subscribe(): ConcurrentLinkedQueue<Message> {

        val options = MqttConnectOptions()
        options.isCleanSession = false
        options.isAutomaticReconnect = true
        options.userName = username
        options.keepAliveInterval = keepAliveInterval
        options.password = password.toCharArray()

        client = MqttClient(broker, clientId, MemoryPersistence())

        client.setCallback(object : MqttCallback {

            override fun connectionLost(cause: Throwable) {
                logger.error("A kapcsolat megszakadt az MQTT kiszolgálóval", cause)
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                val msg = Message(topic, String(message.payload), message.isRetained)
                logger.debug("Message: {}", msg)
                queue.add(msg)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {
                //
            }
        })

        client.connect(options)

        client.subscribe("#", qos)

        return queue
    }

    companion object {
        val logger = LoggerFactory.getLogger(Mqtt::class.java)!!
    }

}