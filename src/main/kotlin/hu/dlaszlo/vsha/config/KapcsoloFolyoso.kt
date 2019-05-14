package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.GpioService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class KapcsoloFolyoso : AbstractDeviceConfig() {

    @Autowired
    lateinit var gpioService: GpioService

    final val mqttName = "folyoso-kapcsolo"
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
                action(KapcsoloFolyoso::getState)
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
                    actionTimeout(KapcsoloFolyoso::powerOff, seconds(30))
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
                clearTimeout(KapcsoloFolyoso::powerOff)
            }
        }

        subscribe {
            topic = "cmnd/folyoso-kapcsolo-topic/POWER"
            payload = "TOGGLE"
            handler = {
                logger.info("dupla érintéssel a konyha kapcsolók kapcsolása")
                action(KapcsoloKonyha::toggle)
            }
        }

        subscribe {
            topic = "cmnd/folyoso-kapcsolo-topic/POWER"
            payload = "HOLD"
            handler = {
                logger.info("hosszú érintéssel a folyosó lámpa bekapcsolása")
                clearTimeout(KapcsoloFolyoso::powerOff)
                longPressPowerOn = if (statePowerOn) {
                    action(KapcsoloFolyoso::powerOff)
                    false
                } else {
                    action(KapcsoloFolyoso::powerOn)
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
        KonnektorNappali.logger.info("A $name átkapcsolása")
        publish("cmnd/$mqttName/power", "TOGGLE", false)
    }

    companion object {
        val logger = LoggerFactory.getLogger(KapcsoloFolyoso::class.java)!!
    }


}