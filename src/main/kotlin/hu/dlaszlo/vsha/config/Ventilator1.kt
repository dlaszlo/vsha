package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.springframework.stereotype.Component

@Component("ventilator1")
open class Ventilator1 : AbstractDeviceConfig() {

    override var device = device {

        mqttName = "ventilator1"
        name = "Konyha ventilator ($mqttName)"

        var turnOnDevice: String? = null
        var lastTurnOff = 0L

        initialize {
            actionCron("ventilator1", "powerOn", "0 0 * * * *")
            actionCron("ventilator1", "powerOff", "0 5 * * * *")
        }

        route {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                actionNow("$mqttName", "getState")
            }
        }

        route {
            topic = "tele/$mqttName/LWT"
            payload = "Offline"
            handler = {
                logger.info("offline")
            }
        }

        route {
            topic = "stat/$mqttName/RESULT"
            payload = "ON"
            jsonPath = "$.POWER"
            handler = {
                logger.info("bekapcsolt")
            }
        }

        route {
            topic = "stat/$mqttName/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER"
            handler = {
                logger.info("kikapcsolt")
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
            id = "powerOn"
            allow = { callerDeviceId ->
                currentTime() - lastTurnOff > minutes(5)
                        && (turnOnDevice == null || callerDeviceId == "ventilator1")
            }
            handler = { callerDeviceId ->
                logger.info("bekapcsolás")
                turnOnDevice = callerDeviceId
                publish("cmnd/$mqttName/power", "ON", false)
            }
        }

        action {
            id = "powerOff"
            allow = { callerDeviceId ->
                turnOnDevice == callerDeviceId
            }
            handler = {
                logger.info("kikapcsolás")
                turnOnDevice = null
                lastTurnOff = currentTime()
                publish("cmnd/$mqttName/power", "OFF", false)
            }
        }

    }
}
