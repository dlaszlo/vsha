package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import hu.dlaszlo.vsha.device.SmsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component("vizelfolyaserzekelo1")
open class VizelfolyasErzekelo1 : AbstractDeviceConfig() {

    @Autowired
    lateinit var smsService: SmsService

    override var device: Device = device {

        mqttName = "rfbridge2"
        name = "Vízelfolyás érzékelő - mosogatógép ($mqttName)"

        var lastSms = 0L

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                actionNow("vizelfolyaserzekelo1", "getState")
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
                actionNow("vizelfolyaserzekelo1", "sendSms")
            }
        }

        action {
            id = "getState"
            handler = {
                logger.info("státusz lekérdezése")
                publish("cmnd/$mqttName/state", "", false)
            }
        }

        action {
            id = "sendSms"
            allow = { currentTime() - lastSms > minutes(15) }
            handler = {
                logger.info("SMS küldése")
                lastSms = currentTime()
                smsService.sendSms("Riasztás! Vízelfolyás érzékelő (mosogatógép).")
            }
        }

    }

}
