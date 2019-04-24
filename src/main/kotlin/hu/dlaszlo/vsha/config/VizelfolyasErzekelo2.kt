package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import hu.dlaszlo.vsha.device.SmsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component("vizelfolyaserzekelo2")
open class VizelfolyasErzekelo2 : AbstractDeviceConfig() {

    @Autowired
    lateinit var smsService: SmsService

    override var device: Device = device {

        mqttName = "rfbridge2"
        name = "Vízelfolyás érzékelő - mosógép ($mqttName)"

        var lastSms = 0L

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                actionNow("vizelfolyaserzekelo2", "getState")
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
            payload = "540412"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("Riasztás! Vízelfolyás érzékelő (mosógép).")
                actionNow("vizelfolyaserzekelo2", "sendSms")
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
                smsService.sendSms("Riasztás! Vízelfolyás érzékelő (mosógép).")
            }
        }

    }

}
