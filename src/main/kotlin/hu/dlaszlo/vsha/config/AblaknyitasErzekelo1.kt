package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.Arrays.asList

@Component("ablaknyitaserzekelo1")
class AblaknyitasErzekelo1 : AbstractDeviceConfig() {

    final val mqttName = "rfbridge2"
    final val name = "Konyha ablaknyitás érzékelő ($mqttName)"

    var online = false
    var windowOpened = false

    override var device: Device = device {

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                online = true
                action(AblaknyitasErzekelo1::getState)
            }
        }

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Offline"
            handler = {
                logger.info("offline")
                online = false
            }
        }

        subscribe {
            topicList = asList("tele/rfbridge1/RESULT", "tele/rfbridge2/RESULT")
            payload = "E1860A"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("ablak kinyitva")
                windowOpened = true
                action(Ventilator1::powerOn)
                actionTimeout(Ventilator1::powerOff, minutes(5))
            }
        }

        subscribe {
            topicList = asList("tele/rfbridge1/RESULT", "tele/rfbridge2/RESULT")
            payload = "E1860E"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("ablak becsukva")
                windowOpened = false
                action(Ventilator1::powerOff)
            }
        }

    }

    fun getState() {
        logger.info("státusz lekérdezése")
        publish("cmnd/$mqttName/state", "", false)
    }

    companion object {
        val logger = LoggerFactory.getLogger(AblaknyitasErzekelo1::class.java)!!
    }
}