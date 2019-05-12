package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.GpioService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component("kapcsolo3")
class Kapcsolo3 : AbstractDeviceConfig() {

    @Autowired
    lateinit var gpioService: GpioService

    final val mqttName = "kapcsolo3"
    final val name = "Konyha lámpakapcsoló ($mqttName)"

    var longPressPowerOn1 = false
    var longPressPowerOn2 = false
    var stateOnline = false
    var statePowerOn1 = false
    var statePowerOn2 = false

    override var device = device {

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                stateOnline = true
                action(Kapcsolo3::getState)
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
            jsonPath = "$.POWER1"
            handler = {
                logger.info("konyha lámpa bekapcsolt")
                statePowerOn1 = true
                if (!longPressPowerOn1) {
                    actionTimeout(Kapcsolo3::powerOff1, minutes(30))
                }
            }
        }

        subscribe {
            topic = "stat/$mqttName/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER1"
            handler = {
                logger.info("konyha lámpa kikapcsolt")
                statePowerOn1 = false
                longPressPowerOn1 = false
                clearTimeout(Kapcsolo3::powerOff1)
            }
        }

        subscribe {
            topic = "stat/$mqttName/RESULT"
            payload = "ON"
            jsonPath = "$.POWER2"
            handler = {
                logger.info("konyhapult lámpa bekapcsolt")
                statePowerOn2 = true
                if (!longPressPowerOn2) {
                    actionTimeout(Kapcsolo3::powerOff2, minutes(30))
                }
            }
        }

        subscribe {
            topic = "stat/$mqttName/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER2"
            handler = {
                logger.info("konyhapult lámpa kikapcsolt")
                statePowerOn2 = false
                longPressPowerOn2 = false
                clearTimeout(Kapcsolo3::powerOff2)
            }
        }

        subscribe {
            topic = "cmnd/kapcsolo3topic/POWER1"
            payload = "TOGGLE"
            handler = {
                Kapcsolo1.logger.info("dupla érintéssel a folyosó lámpa kapcsolása")
                action(Kapcsolo2::toggle)
            }
        }

        subscribe {
            topic = "cmnd/kapcsolo3topic/POWER2"
            payload = "TOGGLE"
            handler = {
                Kapcsolo1.logger.info("dupla érintéssel a folyosó lámpa kapcsolása")
                action(Kapcsolo2::toggle)
            }
        }

        subscribe {
            topic = "cmnd/kapcsolo3topic/POWER1"
            payload = "HOLD"
            handler = {
                logger.info("hosszú érintéssel a konyha lámpa bekapcsolása")
                clearTimeout(Kapcsolo3::powerOff1)
                longPressPowerOn1 = if (statePowerOn1) {
                    action(Kapcsolo3::powerOff1)
                    false
                } else {
                    action(Kapcsolo3::powerOn1)
                    gpioService.beep(100)
                    true
                }
            }
        }

        subscribe {
            topic = "cmnd/kapcsolo3topic/POWER2"
            payload = "HOLD"
            handler = {
                logger.info("hosszú érintéssel a konyhapult lámpa bekapcsolása")
                clearTimeout(Kapcsolo3::powerOff2)
                longPressPowerOn2 = if (statePowerOn2) {
                    action(Kapcsolo3::powerOff2)
                    false
                } else {
                    action(Kapcsolo3::powerOn2)
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

    fun powerOn1() {
        logger.info("konyha lámpa bekapcsolás")
        publish("cmnd/$mqttName/power1", "ON", false)
    }

    fun powerOff1() {
        logger.info("konyha lámpa kikapcsolás")
        publish("cmnd/$mqttName/power1", "OFF", false)
    }

    fun powerOn2() {
        logger.info("konyhapult lámpa bekapcsolás")
        publish("cmnd/$mqttName/power2", "ON", false)
    }

    fun powerOff2() {
        logger.info("konyhapult lámpa kikapcsolás")
        publish("cmnd/$mqttName/power2", "OFF", false)
    }

    fun toggle() {
        Konnektor1.logger.info("A $name átkapcsolása")
        if (statePowerOn1 || statePowerOn2)
        {
            // mindent lekapcsolunk
            if (statePowerOn1) {
                publish("cmnd/$mqttName/power1", "OFF", false)
            }
            if (statePowerOn2) {
                publish("cmnd/$mqttName/power2", "OFF", false)
            }
        }
        else
        {
            // de csak a konyhapultot kapcsoljuk fel
            publish("cmnd/$mqttName/power2", "ON", false)
        }
    }


    companion object {
        val logger = LoggerFactory.getLogger(Kapcsolo3::class.java)!!
    }


}