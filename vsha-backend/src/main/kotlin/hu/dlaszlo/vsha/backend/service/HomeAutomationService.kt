package hu.dlaszlo.vsha.backend.service

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.backend.device.BeeperService
import hu.dlaszlo.vsha.backend.device.Switch
import hu.dlaszlo.vsha.backend.graphql.SubscriptionResolver
import hu.dlaszlo.vsha.mqtt.service.MqttService
import org.influxdb.InfluxDB
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.health.Status
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class HomeAutomationService : Runnable, HealthIndicator {

    @Autowired
    lateinit var context: ApplicationContext

    @Autowired
    lateinit var mqttService: MqttService

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

    @Volatile
    var health: Health = Health.up().build()

    override fun run() {

        try {

            logger.info("start")

            beeperService.beep(100)

            if (influxDB != null) {
                influxDB!!.setDatabase(database)
                influxDB!!.setRetentionPolicy(retentionPolicy)
            }

            val deviceMap = context.getBeansOfType(AbstractDeviceConfig::class.java)
            for (deviceEntry in deviceMap) {
                deviceEntry.value.device.deviceId = deviceEntry.key
                deviceEntry.value.device.initialize()
            }

            val deviceList = deviceMap.values

            while (true) {

                var delay = true

                if (!mqttService.queue.isEmpty()) {
                    delay = false

                    val message = mqttService.queue.poll()

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
                                || subscribe.topicList != null && subscribe.topicList!!.any {
                                    it.equals(
                                        message.topic,
                                        true
                                    )
                                }
                            ) {
                                if (subscribe.jsonPath != null) {
                                    try {
                                        val pl = JsonPath.parse(message.payload).read(subscribe.jsonPath) as String
                                        if (subscribe.payload == null && subscribe.payloadList.isNullOrEmpty()
                                            || subscribe.payload != null && subscribe.payload.equals(pl, true)
                                            || subscribe.payloadList != null && subscribe.payloadList!!.any {
                                                it.equals(
                                                    pl,
                                                    true
                                                )
                                            }
                                        ) {
                                            subscribe.handler(pl)
                                            if (device is Switch) {
                                                subscriptionResolver.updateDeviceInfo(
                                                    device.device.deviceId,
                                                    device.switchState
                                                )
                                            }
                                        }
                                    } catch (e: PathNotFoundException) {
                                        //
                                    }
                                } else {
                                    if (subscribe.payload == null && subscribe.payloadList.isNullOrEmpty()
                                        || subscribe.payload != null && subscribe.payload.equals(message.payload, true)
                                        || subscribe.payloadList != null && subscribe.payloadList!!.any {
                                            it.equals(
                                                message.payload,
                                                true
                                            )
                                        }
                                    ) {
                                        subscribe.handler(message.payload)
                                        if (device is Switch) {
                                            subscriptionResolver.updateDeviceInfo(
                                                device.device.deviceId,
                                                device.switchState
                                            )
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
            }

        } catch (e: RuntimeException) {
            logger.error("Error occured", e)
            health = Health.down(e).build()
        }
    }

    override fun health(): Health {
        return health
    }

    companion object {
        val logger = LoggerFactory.getLogger(HomeAutomationService::class.java)!!
    }

}