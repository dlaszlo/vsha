package hu.dlaszlo.vsha

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.BeeperService
import hu.dlaszlo.vsha.device.Switch
import hu.dlaszlo.vsha.graphql.SubscriptionResolver
import hu.dlaszlo.vsha.mqtt.Mqtt
import org.eclipse.paho.client.mqttv3.MqttException
import org.influxdb.InfluxDB
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class HomeAutomationService : Runnable {

    @Autowired
    @Qualifier("main_executor")
    lateinit var taskExecutor: TaskExecutor

    @Autowired
    lateinit var mqtt: Mqtt

    @Autowired
    lateinit var context: ApplicationContext

    @Autowired
    lateinit var subscriptionResolver: SubscriptionResolver

    @Autowired
    lateinit var beeperService: BeeperService

    @Autowired(required = false)
    var influxDB: InfluxDB? = null

    @Value("\${spring.influx.database:}")
    var database: String? = null

    @Value("\${spring.influx.retentionPolicy:}")
    var retentionPolicy: String? = null

    override fun run() {

        beeperService.beep(500)

        if (influxDB != null) {
            influxDB!!.setDatabase(database)
            influxDB!!.setRetentionPolicy(retentionPolicy)
        }

        val queue = mqtt.connect()

        val deviceMap = context.getBeansOfType(AbstractDeviceConfig::class.java)
        for (deviceEntry in deviceMap) {
            deviceEntry.value.device.deviceId = deviceEntry.key
            deviceEntry.value.device.initialize()
        }

        val deviceList = deviceMap.values

        while (true) {
            try {
                var delay = true

                if (!queue.isEmpty()) {
                    delay = false

                    val message = queue.poll()

                    logger.info(
                            "         MQTT: topic = {}, payload = {}, retained = {}",
                            message.topic,
                            message.payload,
                            message.isRetained
                    )

                    for (device in deviceList) {
                        for (subscribe in device.device.subscribeList) {
                            if (subscribe.topic == null && subscribe.topicList.isNullOrEmpty()
                                    || subscribe.topic != null && subscribe.topic.equals(message.topic, true)
                                    || subscribe.topicList != null && subscribe.topicList!!.any { it.equals(message.topic, true) }
                            ) {
                                if (subscribe.jsonPath != null) {
                                    try {
                                        val pl = JsonPath.parse(message.payload).read(subscribe.jsonPath) as String
                                        if (subscribe.payload == null && subscribe.payloadList.isNullOrEmpty()
                                                || subscribe.payload != null && subscribe.payload.equals(pl, true)
                                                || subscribe.payloadList != null && subscribe.payloadList!!.any { it.equals(pl, true) }
                                        ) {
                                            subscribe.handler(pl)
                                            if (device is Switch) {
                                                subscriptionResolver.updateDeviceInfo(device.device.deviceId,
                                                        device.switchState.name,
                                                        device.switchState.online,
                                                        device.switchState.powerOn)
                                            }
                                        }
                                    } catch (e: PathNotFoundException) {
                                        //
                                    }
                                } else {
                                    if (subscribe.payload == null && subscribe.payloadList.isNullOrEmpty()
                                            || subscribe.payload != null && subscribe.payload.equals(message.payload, true)
                                            || subscribe.payloadList != null && subscribe.payloadList!!.any { it.equals(message.payload, true) }
                                    ) {
                                        subscribe.handler(message.payload)
                                        if (device is Switch) {
                                            subscriptionResolver.updateDeviceInfo(device.device.deviceId,
                                                    device.switchState.name,
                                                    device.switchState.online,
                                                    device.switchState.powerOn)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                for (device in deviceList) {
                    val iterator = device.schedulerList.iterator()
                    while (iterator.hasNext()) {
                        val scheduler = iterator.next()
                        if (scheduler.runAction()) {
                            iterator.remove()
                        }
                    }
                }

                if (delay) {
                    Thread.sleep(100)
                }

            } catch (e: MqttException) {
                logger.error("MQTT hiba történt, újracsatlakozás 30s után: {} ({})", e.message, e.reasonCode)
                Thread.sleep(30000)
                try {
                    mqtt.reconnect()
                } catch (ex: MqttException) {
                    logger.error("MQTT hiba történt: {} ({})", e.message, e.reasonCode)
                }
            }
        }
    }

    @PostConstruct
    fun init() {
        taskExecutor.execute(this)
    }

}