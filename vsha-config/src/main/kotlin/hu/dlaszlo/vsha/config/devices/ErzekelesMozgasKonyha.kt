package hu.dlaszlo.vsha.config.devices

import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("erzekeloMozgasKonyha")
class ErzekeloMozgasKonyha : AbstractDeviceConfig() {

    data class DeviceState(
        val mqttName1: String = "konyha-rfbridge",
        val mqttName2: String = "nappali-rfbridge",
        val name: String = "Mozgásérzékelő"
    )

    val state = DeviceState()

    override var device = device {

        subscribe {
            topicList = listOf("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payload = "EF78EE"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("mozgás észlelve")
                if (!sunsetSunriseService.isDaylight()
                    && sunsetSunriseService.isAfter(12, 0)
                ) {
                    action(KapcsoloKonyhapult::automaticPowerOn)
                }
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(ErzekeloMozgasKonyha::class.java)!!
    }
}