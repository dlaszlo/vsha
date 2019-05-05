package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("ventilator1")
class Ventilator1 : AbstractDeviceConfig() {

    final val mqttName = "ventilator1"
    final val name = "Konyha ventilator ($mqttName)"

    var lastTurnOff = 0L
    var scheduledTurnedOn = false

    override var device = device {

        initialize {
            actionCron(Ventilator1::scheduledPowerOn, "0 0 * * * *")
            actionCron(Ventilator1::scheduledPowerOff, "0 5 * * * *")
        }

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                action(Ventilator1::getState)
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
            topic = "stat/$mqttName/RESULT"
            payload = "ON"
            jsonPath = "$.POWER"
            handler = {
                logger.info("bekapcsolt")
            }
        }

        subscribe {
            topic = "stat/$mqttName/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER"
            handler = {
                logger.info("kikapcsolt")
            }
        }
    }

    fun getState() {
        logger.info("státusz lekérdezése")
        publish("cmnd/$mqttName/state", "", false)
    }

    fun powerOn() {
        if (!scheduledTurnedOn && currentTime() - lastTurnOff > minutes(5)) {
            logger.info("bekapcsolás")
            publish("cmnd/$mqttName/power", "ON", false)
        }
    }

    fun powerOff() {
        if (!scheduledTurnedOn) {
            logger.info("kikapcsolás")
            lastTurnOff = currentTime()
            publish("cmnd/$mqttName/power", "OFF", false)
        }
    }

    fun scheduledPowerOn() {
        if (currentTime() - lastTurnOff > minutes(5)) {
            scheduledTurnedOn = true
            logger.info("időzített bekapcsolás")
            publish("cmnd/$mqttName/power", "ON", false)
        }
    }

    fun scheduledPowerOff() {
        logger.info("időzített kikapcsolás")
        lastTurnOff = currentTime()
        scheduledTurnedOn = false
        publish("cmnd/$mqttName/power", "OFF", false)
    }

    companion object {
        val logger = LoggerFactory.getLogger(Ventilator1::class.java)!!
    }

}
