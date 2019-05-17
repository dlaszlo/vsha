package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import hu.dlaszlo.vsha.sms.SmsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.Arrays.asList

@Component
class Csengo : AbstractDeviceConfig() {

    @Autowired
    lateinit var smsService: SmsService

    final val mqttName1 = "konyha-rfbridge"
    final val mqttName2 = "nappali-rfbridge"
    final val name = "Csengő ($mqttName1, $mqttName2)"

    var lastSms = 0L

    override var device: Device = device {

        subscribe {
            topicList = asList("tele/$mqttName1/RESULT", "tele/$mqttName2/RESULT")
            payload = "41E021"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("Csengetett valaki.")
                action(Csengo::sendSms)
            }
        }
    }

    fun sendSms() {
        if (currentTime() - lastSms > minutes(1)) {
            logger.info("SMS küldése")
            lastSms = currentTime()
            smsService.sendSms("Csengetett valaki.")
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(Csengo::class.java)!!
    }


}
