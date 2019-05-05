package hu.dlaszlo.vsha

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import hu.dlaszlo.vsha.device.AbstractDeviceConfig
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

    @Autowired(required = false)
    var influxDB: InfluxDB? = null

    @Value("\${spring.influx.database:}")
    var database: String? = null

    @Value("\${spring.influx.retentionPolicy:}")
    var retentionPolicy: String? = null

    override fun run() {

        banner()

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
                            if (subscribe.topic == null
                                || subscribe.topic.equals(message.topic, true)
                            ) {
                                if (subscribe.jsonPath != null) {
                                    try {
                                        val pl = JsonPath.parse(message.payload).read(subscribe.jsonPath) as String
                                        if (subscribe.payload == null || subscribe.payload.equals(pl, true)) {
                                            subscribe.handler(pl)
                                        }
                                    } catch (e: PathNotFoundException) {
                                        //
                                    }
                                } else {
                                    if (subscribe.payload == null || subscribe.payload.equals(message.payload, true))
                                        subscribe.handler(message.payload)
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

    fun banner() {
        logger.info("")
        logger.info("  _  _                   _       _                  _   _                    ")
        logger.info(" | || |___ _ __  ___    /_\\ _  _| |_ ___ _ __  __ _| |_(_)___ _ _           ")
        logger.info(" | __ / _ \\ '  \\/ -_)  / _ \\ || |  _/ _ \\ '  \\/ _` |  _| / _ \\ ' \\    ")
        logger.info(" |_||_\\___/_|_|_\\___| /_/ \\_\\_,_|\\__\\___/_|_|_\\__,_|\\__|_\\___/_||_| ")
        logger.info("                                                        version 1.0")
        logger.info("")
    }

    @PostConstruct
    fun init() {
        taskExecutor.execute(this)
    }

}