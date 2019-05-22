package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.sunsetsunrise.SunsetSunriseService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.Arrays.asList

@Component("erzekeloMozgasFolyoso")
class ErzekeloMozgasFolyoso : AbstractDeviceConfig() {

    @Autowired
    lateinit var sunsetSunriseService: SunsetSunriseService

    data class DeviceState (
        val mqttName1: String = "konyha-rfbridge",
        val mqttName2: String = "nappali-rfbridge",
        val name: String = "Folyosó mozgásérzékelő ($mqttName1, $mqttName2)"
    )

    val state = DeviceState()

    override var device = device {

        subscribe {
            topicList = asList("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payload = "EC27FE"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("mozgás észlelve")
                if (!sunsetSunriseService.isDaylight()) {
                    action(KapcsoloFolyoso::powerOn)
                }
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(ErzekeloMozgasFolyoso::class.java)!!
    }
}