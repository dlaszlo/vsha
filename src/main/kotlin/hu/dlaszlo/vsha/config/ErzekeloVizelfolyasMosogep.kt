package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import hu.dlaszlo.vsha.device.SmsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.Arrays.asList

@Component
class ErzekeloVizelfolyasMosogep : AbstractDeviceConfig() {

    @Autowired
    lateinit var smsService: SmsService

    final val mqttName1 = "konyha-rfbridge"
    final val mqttName2 = "nappali-rfbridge"
    final val name = "Vízelfolyás érzékelő - mosógép ($mqttName1)"

    var lastSms = 0L

    override var device: Device = device {

        subscribe {
            topicList = asList("tele/$mqttName1/RESULT", "tele/$mqttName2/RESULT")
            payload = "540412"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("Riasztás! Vízelfolyás érzékelő (mosógép).")
                action(ErzekeloVizelfolyasMosogep::sendSms)
            }
        }
    }

    fun sendSms() {
        if (currentTime() - lastSms > minutes(1)) {
            logger.info("SMS küldése")
            lastSms = currentTime()
            smsService.sendSms("Riasztás! Vízelfolyás érzékelő (mosógép).")
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(ErzekeloVizelfolyasMosogep::class.java)!!
    }


}
