package hu.dlaszlo.vsha

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.mqtt.Mqtt
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext


@SpringBootApplication
open class HomeAutomation : CommandLineRunner {

    @Autowired
    lateinit var mqtt: Mqtt

    @Autowired
    lateinit var context: ApplicationContext

    override fun run(vararg args: String?) {

        val queue = mqtt.subscribe()

        val deviceList = context.getBeansOfType(AbstractDeviceConfig::class.java).values

        for (device in deviceList) {
            device.device.logger = LoggerFactory.getLogger(device.javaClass)
            if (device.device.initialize != null) {
                device.device.initialize!!.invoke()
            }
        }

        while (true) {

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
                    for (route in device.device.routeList) {
                        if (route.topic == null
                            || route.topic.equals(message.topic, true)
                        ) {
                            if (route.jsonPath != null) {
                                try {
                                    val pl = JsonPath.parse(message.payload).read(route.jsonPath) as String
                                    if (route.payload == null || route.payload.equals(pl, true)) {
                                        route.handler!!.invoke(pl)
                                    }
                                } catch (e: PathNotFoundException) {
                                    //
                                }
                            } else {
                                if (route.payload == null || route.payload.equals(message.payload, true))
                                route.handler!!.invoke(message.payload)
                            }
                        }
                    }
                }
            }

            for (device in deviceList) {
                val iterator = device.schedulerList.iterator()
                while (iterator.hasNext()) {
                    val scheduler = iterator.next()
                    if (scheduler.action()) {
                        iterator.remove()
                    }
                }
            }

            if (delay) {
                Thread.sleep(100)
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(HomeAutomation::class.java)!!
    }

}

fun main(args: Array<String>) {
    runApplication<HomeAutomation>(*args)
}
