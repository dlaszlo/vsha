package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import hu.dlaszlo.vsha.device.SmsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.Arrays.asList

@Component("csengo")
class Csengo : AbstractDeviceConfig() {

    @Autowired
    lateinit var smsService: SmsService

    final val mqttName = "rfbridge1"
    final val name = "Csengő ($mqttName)"

    var lastSms = 0L

    override var device: Device = device {

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                action(Csengo::getState)
            }
        }

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Offline"
            handler = {
                logger.info("offline")
            }
        }

        subscribe {
            topicList = asList("tele/rfbridge1/RESULT", "tele/rfbridge2/RESULT")
            payload = "41E021"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("Csengetett valaki.")
                action(Csengo::sendSms)
            }
        }
    }

    fun getState() {
        logger.info("státusz lekérdezése")
        publish("cmnd/$mqttName/state", "", false)
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
