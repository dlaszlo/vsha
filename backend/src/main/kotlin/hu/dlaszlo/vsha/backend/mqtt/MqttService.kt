package hu.dlaszlo.vsha.backend.mqtt

import hu.dlaszlo.vsha.backend.BackendConfiguration
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.mqtt.support.MqttHeaders
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentLinkedQueue
import hu.dlaszlo.vsha.backend.mqtt.Message as MqttMessage

@Component
class MqttService {

    @Value("\${mqtt.qos}")
    private var qos: Int = 0

    val queue: ConcurrentLinkedQueue<MqttMessage> = ConcurrentLinkedQueue()

    @Autowired
    lateinit var gateway: BackendConfiguration.MqttGateway

    fun publish(topic: String, payload: String, retained: Boolean) {
        gateway.send(topic, retained, qos, payload)
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    fun handleMessage(message: Message<*>) {
        val msg = MqttMessage(
            message.headers[MqttHeaders.RECEIVED_TOPIC] as String,
            message.payload as String,
            message.headers[MqttHeaders.RECEIVED_RETAINED] as Boolean
        )
        logger.debug(msg.toString())
        queue.add(msg)
    }

    companion object {
        val logger = LoggerFactory.getLogger(BackendConfiguration::class.java)!!
    }

}