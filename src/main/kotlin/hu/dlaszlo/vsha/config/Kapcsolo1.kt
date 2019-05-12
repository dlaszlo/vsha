package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.GpioService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component("kapcsolo1")
class Kapcsolo1 : AbstractDeviceConfig() {

    @Autowired
    lateinit var gpioService: GpioService

    final val mqttName = "kapcsolo1"
    final val name = "Nappali lámpakapcsoló ($mqttName)"

    var longPressPowerOn = false
    var stateOnline = false
    var statePowerOn = false

    override var device = device {
        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                stateOnline = true
                action(Kapcsolo1::getState)
            }
        }

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Offline"
            handler = {
                logger.info("offline")
                stateOnline = false
            }
        }

        subscribe {
            topic = "stat/$mqttName/RESULT"
            payload = "ON"
            jsonPath = "$.POWER"
            handler = {
                logger.info("bekapcsolt")
                statePowerOn = true
                if (!longPressPowerOn) {
                    actionTimeout(Kapcsolo1::powerOff, minutes(5))
                }
            }
        }

        subscribe {
            topic = "stat/$mqttName/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER"
            handler = {
                logger.info("kikapcsolt")
                statePowerOn = false
                longPressPowerOn = false
                clearTimeout(Kapcsolo1::powerOff)
            }
        }

        subscribe {
            topic = "cmnd/kapcsolo1topic/POWER"
            payload = "TOGGLE"
            handler = {
                logger.info("dupla érintéssel a nappali állólámpa kapcsolása")
                action(Konnektor1::toggle)
            }
        }


        subscribe {
            topic = "cmnd/kapcsolo1topic/POWER"
            payload = "HOLD"
            handler = {
                logger.info("hosszú érintéssel a nappali lámpa bekapcsolása")
                clearTimeout(Kapcsolo1::powerOff)
                longPressPowerOn = if (statePowerOn) {
                    action(Kapcsolo1::powerOff)
                    false
                } else {
                    action(Kapcsolo1::powerOn)
                    gpioService.beep(100)
                    true
                }
            }
        }

    }

    fun getState() {
        logger.info("státusz lekérdezése")
        publish("cmnd/$mqttName/state", "", false)
    }

    fun powerOn() {
        logger.info("bekapcsolás")
        publish("cmnd/$mqttName/power", "ON", false)
    }

    fun powerOff() {
        logger.info("kikapcsolás")
        publish("cmnd/$mqttName/power", "OFF", false)
    }

    companion object {
        val logger = LoggerFactory.getLogger(Kapcsolo1::class.java)!!
    }


}
