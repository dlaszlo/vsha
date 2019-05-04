package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("kapcsolo2")
open class Kapcsolo2 : AbstractDeviceConfig() {

    val mqttName = "kapcsolo2"
    val name = "Folyosó lámpakapcsoló ($mqttName)"

    override var device = device {

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                actionNow<Kapcsolo2>("getState") { device ->
                    device.getState()
                }
            }
        }

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Offline"
            handler = {
                logger.info("offline")
            }
        }

        subscribe {
            topic = "stat/$mqttName/RESULT"
            payload = "ON"
            jsonPath = "$.POWER"
            handler = {
                logger.info("bekapcsolt")
            }
        }

        subscribe {
            topic = "stat/$mqttName/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER"
            handler = {
                logger.info("kikapcsolt")
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
        val logger = LoggerFactory.getLogger(Kapcsolo2::class.java)!!
    }


}