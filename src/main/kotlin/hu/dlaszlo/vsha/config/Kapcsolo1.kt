package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("kapcsolo1")
open class Kapcsolo1 : AbstractDeviceConfig() {
    val mqttName = "kapcsolo1"
    val name = "Nappali lámpakapcsoló ($mqttName)"

    override var device = device {
        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                actionNow<Kapcsolo1>("getState") { device ->
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
                actionTimeout<Kapcsolo1>("kapcsolo1", minutes(5)) { device ->
                    device.powerOff()
                }
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

        subscribe {
            topic = "cmnd/kapcsolo1topic/POWER"
            payload = "TOGGLE"
            handler = {
                logger.info("dupla érintéssel a nappali állólámpa kapcsolása")
                actionNow<Konnektor1>("konnektor1") { device ->
                    device.toggle()
                }
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
        val logger = LoggerFactory.getLogger(Kapcsolo1::class.java)!!
    }


}
