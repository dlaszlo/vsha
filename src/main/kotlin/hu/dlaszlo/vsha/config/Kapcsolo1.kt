package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.springframework.stereotype.Component

@Component("kapcsolo1")
open class Kapcsolo1 : AbstractDeviceConfig() {

    override var device = device {

        mqttName = "kapcsolo1"
        name = "Nappali lámpakapcsoló ($mqttName)"

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                actionNow("$mqttName", "getState")
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
                actionTimeout("kapcsolo1", "powerOff", minutes(5))
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

        subscribe {
            topic = "cmnd/kapcsolo1topic/POWER"
            payload = "TOGGLE"
            handler = {
                logger.info("dupla érintéssel a nappali állólámpa kapcsolása")
                actionNow("konnektor1", "toggle")
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
            handler = {
                logger.info("bekapcsolás")
                publish("cmnd/$mqttName/power", "ON", false)
            }
        }

        action {
            id = "powerOff"
            handler = {
                logger.info("kikapcsolás")
                publish("cmnd/$mqttName/power", "OFF", false)
            }
        }

    }
}
