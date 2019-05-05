package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import hu.dlaszlo.vsha.device.SmsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component("vizelfolyaserzekelo1")
class VizelfolyasErzekelo1 : AbstractDeviceConfig() {

    @Autowired
    lateinit var smsService: SmsService

    final val mqttName = "rfbridge2"
    final val name = "Vízelfolyás érzékelő - mosogatógép ($mqttName)"

    var lastSms = 0L

    override var device: Device = device {

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                action(VizelfolyasErzekelo1::getState)
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
            topic = "tele/$mqttName/RESULT"
            payload = "330312"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("Riasztás! Vízelfolyás érzékelő (mosogatógép).")
                action(VizelfolyasErzekelo1::sendSms)
            }
        }
    }

    fun getState() {
        logger.info("státusz lekérdezése")
        publish("cmnd/$mqttName/state", "", false)
    }

    fun sendSms() {
        logger.info("SMS küldése")
        lastSms = currentTime()
        smsService.sendSms("Riasztás! Vízelfolyás érzékelő (mosogatógép).")
    }

    companion object {
        val logger = LoggerFactory.getLogger(VizelfolyasErzekelo1::class.java)!!
    }


}
