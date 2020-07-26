package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.Arrays.asList

@Component("erzekeloMozgasKonyha")
class ErzekeloMozgasKonyha : AbstractDeviceConfig() {

    data class DeviceState(
            val mqttName1: String = "konyha-rfbridge",
            val mqttName2: String = "nappali-rfbridge",
            val name: String = "Konyha mozgásérzékelő ($mqttName1, $mqttName2)"
    )

    val state = DeviceState()

    override var device = device {

        subscribe {
            topicList = asList("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payload = "EF78EE"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("mozgás észlelve")
                if (!sunsetSunriseService.isDaylight()) {
                    action(KapcsoloKonyhapult::automaticPowerOn)
                }
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(ErzekeloMozgasKonyha::class.java)!!
    }
}