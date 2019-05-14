package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalTime
import java.util.Arrays.asList

@Component
class ErzekeloMozgasFolyoso : AbstractDeviceConfig() {

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
                val hour = LocalTime.now().hour
                if (hour >= 20 || hour < 6) {
                    action(KapcsoloFolyoso::powerOn)
                }
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(ErzekeloMozgasFolyoso::class.java)!!
    }


}