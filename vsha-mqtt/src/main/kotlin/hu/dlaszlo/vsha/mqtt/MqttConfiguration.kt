package hu.dlaszlo.vsha.mqtt

import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.integration.annotation.IntegrationComponentScan
import org.springframework.integration.annotation.MessagingGateway
import org.springframework.integration.annotation.ServiceActivator
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.core.MessageProducer
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory
import org.springframework.integration.mqtt.core.MqttPahoClientFactory
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter
import org.springframework.integration.mqtt.support.MqttHeaders
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.handler.annotation.Header
import java.util.*

@Configuration
@ComponentScan
@IntegrationComponentScan
class MqttConfiguration {

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

    @Value("\${mqtt.completionTimeout}")
    private val completionTimeout: Long = 0

    @Value("\${mqtt.recoveryInterval}")
    private val recoveryInterval: Int = 0

    @Bean
    fun mqttClientFactory(): MqttPahoClientFactory {
        val factory = DefaultMqttPahoClientFactory()
        val options = MqttConnectOptions()
        options.isCleanSession = true
        options.isAutomaticReconnect = true
        options.userName = username
        options.password = password.toCharArray()
        options.keepAliveInterval = keepAliveInterval
        factory.connectionOptions = options
        return factory
    }


    @Bean("mqttInputChannel")
    fun mqttInputChannel(): MessageChannel {
        return DirectChannel()
    }

    @Bean
    fun mqttInbound(@Autowired @Qualifier("mqttInputChannel") mqttInputChannel: MessageChannel): MessageProducer {
        val adapter = MqttPahoMessageDrivenChannelAdapter(
            broker,
            clientId + "_" + UUID.randomUUID().toString(),
            mqttClientFactory(),
            "#"
        )
        adapter.setCompletionTimeout(completionTimeout)
        adapter.setRecoveryInterval(recoveryInterval)
        adapter.setQos(qos)
        adapter.setConverter(DefaultPahoMessageConverter())
        adapter.outputChannel = mqttInputChannel
        return adapter
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    fun mqttOutbound(): MessageHandler {
        val messageHandler =
            MqttPahoMessageHandler(broker, clientId + "_" + UUID.randomUUID().toString(), mqttClientFactory())
        messageHandler.setAsync(true)
        messageHandler.setDefaultTopic("vsha")
        return messageHandler
    }

    @Bean
    fun mqttOutboundChannel(): MessageChannel {
        return DirectChannel()
    }

    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
    interface MqttGateway {
        fun send(
            @Header(MqttHeaders.TOPIC) topic: String,
            @Header(MqttHeaders.RETAINED) retained: Boolean,
            @Header(MqttHeaders.QOS) qos: Int,
            payload: String
        )
    }

    companion object {
        val logger = LoggerFactory.getLogger(MqttConfiguration::class.java)!!
    }

}