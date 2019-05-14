package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import hu.dlaszlo.vsha.device.SmsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.Arrays.asList

@Component
class ErzekeloVizelfolyasMosogatogep : AbstractDeviceConfig() {

    @Autowired
    lateinit var smsService: SmsService

    final val mqttName1 = "konyha-rfbridge"
    final val mqttName2 = "nappali-rfbridge"
    final val name = "Vízelfolyás érzékelő - mosogatógép ($mqttName1)"

    var lastSms = 0L

    override var device: Device = device {

        subscribe {
            topicList = asList("tele/$mqttName1/RESULT", "tele/$mqttName2/RESULT")
            payload = "330312"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("Riasztás! Vízelfolyás érzékelő (mosogatógép).")
                action(ErzekeloVizelfolyasMosogatogep::sendSms)
            }
        }
    }

    fun sendSms() {
        if (currentTime() - lastSms > minutes(1)) {
            logger.info("SMS küldése")
            lastSms = currentTime()
            smsService.sendSms("Riasztás! Vízelfolyás érzékelő (mosogatógép).")
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(ErzekeloVizelfolyasMosogatogep::class.java)!!
    }


}
