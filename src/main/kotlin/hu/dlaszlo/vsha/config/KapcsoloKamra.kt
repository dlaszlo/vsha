package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.GpioService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class KapcsoloKamra : AbstractDeviceConfig() {

    @Autowired
    lateinit var gpioService: GpioService

    final val mqttName = "kamra-kapcsolo"
    final val name = "Kamra lámpakapcsoló ($mqttName)"

    var stateOnline = false
    var statePowerOn = false

    override var device = device {

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                stateOnline = true
                action(KapcsoloKamra::getState)
            }
        }

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Offline"
            handler = {
                logger.info("offline")
                stateOnline = false
            }
        }

        subscribe {
            topic = "stat/$mqttName/RESULT"
            payload = "ON"
            jsonPath = "$.POWER"
            handler = {
                logger.info("bekapcsolt")
                statePowerOn = true
                actionTimeout(KapcsoloKamra::powerOff, minutes(2))
            }
        }

        subscribe {
            topic = "stat/$mqttName/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER"
            handler = {
                logger.info("kikapcsolt")
                statePowerOn = false
                clearTimeout(KapcsoloKamra::powerOff)
            }
        }
    }

    fun getState() {
        logger.info("státusz lekérdezése")
        publish("cmnd/$mqttName/state", "", false)
    }

    fun powerOn() {
        logger.info("bekapcsolás")
        publish("cmnd/$mqttName/power", "ON", false)
    }

    fun powerOff() {
        logger.info("kikapcsolás")
        publish("cmnd/$mqttName/power", "OFF", false)
    }

    companion object {
        val logger = LoggerFactory.getLogger(KapcsoloKamra::class.java)!!
    }


}