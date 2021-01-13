package hu.dlaszlo.vsha.config.devices

import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("erzekeloMozgasFolyoso")
class ErzekeloMozgasFolyoso : AbstractDeviceConfig() {

    data class DeviceState(
        val mqttName1: String = "konyha-rfbridge",
        val mqttName2: String = "nappali-rfbridge",
        val name: String = "Mozgásérzékelő"
    )

    val state = DeviceState()

    override var device = device {

        subscribe {
            topicList = listOf("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payload = "EC27FE"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("mozgás észlelve")
                if (!sunsetSunriseService.isDaylight()
                    && (sunsetSunriseService.isBefore(6, 0) || sunsetSunriseService.isAfter(12, 0))) {
                    action(KapcsoloFolyoso::automaticPowerOn)
                }
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(ErzekeloMozgasFolyoso::class.java)!!
    }
}