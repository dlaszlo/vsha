package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.Arrays.asList

@Component
class ErzekeloAblaknyitasKonyha : AbstractDeviceConfig() {

    final val mqttName1 = "konyha-rfbridge"
    final val mqttName2 = "nappali-rfbridge"

    final val name = "Konyha ablaknyitás érzékelő ($mqttName1, $mqttName2)"

    var online = false
    var windowOpened = false

    override var device: Device = device {

        subscribe {
            topicList = asList("tele/$mqttName1/RESULT", "tele/$mqttName2/RESULT")
            payload = "E1860A"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("ablak kinyitva")
                windowOpened = true
                action(VentilatorKonyha::powerOn)
                actionTimeout(VentilatorKonyha::powerOff, minutes(5))
            }
        }

        subscribe {
            topicList = asList("tele/rfbridge1/RESULT", "tele/rfbridge2/RESULT")
            payload = "E1860E"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("ablak becsukva")
                windowOpened = false
                action(VentilatorKonyha::powerOff)
            }
        }

    }

    companion object {
        val logger = LoggerFactory.getLogger(ErzekeloAblaknyitasKonyha::class.java)!!
    }
}