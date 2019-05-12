package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.GpioService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component("kapcsolo2")
class Kapcsolo2 : AbstractDeviceConfig() {

    @Autowired
    lateinit var gpioService: GpioService

    final val mqttName = "kapcsolo2"
    final val name = "Folyosó lámpakapcsoló ($mqttName)"

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
                action(Kapcsolo2::getState)
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
                    actionTimeout(Kapcsolo2::powerOff, seconds(30))
                }
            }
        }

        subscribe {
            topic = "cmnd/kapcsolo2topic/POWER"
            payload = "TOGGLE"
            handler = {
                logger.info("dupla érintéssel a konyha kapcsolók kapcsolása")
                action(Kapcsolo3::toggle)
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
                clearTimeout(Kapcsolo2::powerOff)
            }
        }

        subscribe {
            topic = "cmnd/kapcsolo2topic/POWER"
            payload = "HOLD"
            handler = {
                logger.info("hosszú érintéssel a folyosó lámpa bekapcsolása")
                clearTimeout(Kapcsolo2::powerOff)
                longPressPowerOn = if (statePowerOn) {
                    action(Kapcsolo2::powerOff)
                    false
                } else {
                    action(Kapcsolo2::powerOn)
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

    fun toggle() {
        Konnektor1.logger.info("A $name átkapcsolása")
        publish("cmnd/$mqttName/power", "TOGGLE", false)
    }

    companion object {
        val logger = LoggerFactory.getLogger(Kapcsolo2::class.java)!!
    }


}