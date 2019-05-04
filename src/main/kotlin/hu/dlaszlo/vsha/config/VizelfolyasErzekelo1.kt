package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import hu.dlaszlo.vsha.device.SmsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component("vizelfolyaserzekelo1")
open class VizelfolyasErzekelo1() : AbstractDeviceConfig() {

    @Autowired
    lateinit var smsService: SmsService

    val mqttName = "rfbridge2"
    val name = "Vízelfolyás érzékelő - mosogatógép ($mqttName)"
    var lastSms = 0L

    override var device: Device = device {

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                actionNow<VizelfolyasErzekelo1>("getState") {device ->
                    device.getState()
                }
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
                actionNow<VizelfolyasErzekelo1>("sendSms") {device ->
                    device.sendSms()
                }
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
