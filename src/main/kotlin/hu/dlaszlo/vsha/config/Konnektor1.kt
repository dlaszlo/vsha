package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("konnektor1")
class Konnektor1 : AbstractDeviceConfig() {

    final val mqttName = "konnektor1"
    final val name = "Nappali állólámpa ($mqttName)"

    override var device = device {

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("A $name online")
                action(Konnektor1::getState)
            }
        }

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Offline"
            handler = {
                logger.info("A $name offline")
            }
        }

        subscribe {
            topic = "stat/$mqttName/RESULT"
            payload = "ON"
            jsonPath = "$.POWER"
            handler = {
                logger.info("A $name bekapcsolt")
            }
        }

        subscribe {
            topic = "stat/$mqttName/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER"
            handler = {
                logger.info("A $name kikapcsolt")
            }
        }
    }

    fun getState() {
        logger.info("Státusz lekérdezése: $name")
        publish("cmnd/$mqttName/state", "", false)
    }

    fun powerOn() {
        logger.info("A $name bekapcsolása")
        publish("cmnd/$mqttName/power", "ON", false)
    }

    fun powerOff() {
        logger.info("A $name kikapcsolása")
        publish("cmnd/$mqttName/power", "OFF", false)
    }

    fun toggle() {
        logger.info("A $name átkapcsolása")
        publish("cmnd/$mqttName/power", "TOGGLE", false)
    }

    companion object {
        val logger = LoggerFactory.getLogger(Konnektor1::class.java)!!
    }


}