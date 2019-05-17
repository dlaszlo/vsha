package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.sunsetsunrise.SunsetSunriseService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.Arrays.asList

@Component
class ErzekeloMozgasFolyoso : AbstractDeviceConfig() {

    @Autowired
    lateinit var sunsetSunriseService: SunsetSunriseService

    final val mqttName1 = "konyha-rfbridge"
    final val mqttName2 = "nappali-rfbridge"
    final val name = "Folyosó mozgásérzékelő ($mqttName1, $mqttName2)"

    override var device = device {

        subscribe {
            topicList = asList("tele/$mqttName1/RESULT", "tele/$mqttName2/RESULT")
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