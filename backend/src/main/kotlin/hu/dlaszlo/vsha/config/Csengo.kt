package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.Arrays.asList

@Component("csengo")
class Csengo : AbstractDeviceConfig() {

    data class DeviceState(
            val mqttName1: String = "konyha-rfbridge",
            val mqttName2: String = "nappali-rfbridge",
            val name: String = "Csengő ($mqttName1, $mqttName2)"
    )

    var state = DeviceState()

    var lastSms = 0L

    override var device: Device = device {

        subscribe {
            topicList = asList("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payload = "41E021"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("Csengetett valaki.")
                action(Csengo::sendSms)
            }
        }
    }

    fun sendSms(): Boolean {
        if (currentTime() - lastSms > minutes(1)) {
            logger.info("SMS küldése")
            lastSms = currentTime()
            telegramService.sendNotification("Csengetett valaki.")
        }
        return true
    }

    companion object {
        val logger = LoggerFactory.getLogger(Csengo::class.java)!!
    }

}
