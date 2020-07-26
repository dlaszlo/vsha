package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.Arrays.asList

@Component("kapcsoloUdvarBelso")
class KapcsoloUdvarBelso : AbstractDeviceConfig() {

    data class DeviceState(
            val mqttName1: String = "konyha-rfbridge",
            val mqttName2: String = "nappali-rfbridge",
            val name: String = "Udvar kapcsoló (belső) ($mqttName1, $mqttName2)"
    )

    var state = DeviceState()

    var lastToggle = 0L

    override var device: Device = device {

        subscribe {
            topicList = asList("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payloadList = asList("B41A64")
            jsonPath = "$.RfReceived.Data"
            handler = {
                if (currentTime() - lastToggle >= seconds(1)) {
                    logger.info("Udvari lámpa átkapcsolása")
                    lastToggle = currentTime()
                    action(KapcsoloUdvar::toggle)
                }
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(KapcsoloUdvarBelso::class.java)!!
    }

}
