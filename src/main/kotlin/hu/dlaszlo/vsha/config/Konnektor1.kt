package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.springframework.stereotype.Component

@Component("konnektor1")
open class Konnektor1 : AbstractDeviceConfig() {

    override var device = device {

        mqttName = "konnektor1"
        name = "Nappali állólámpa ($mqttName)"

        route {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("A $name online")
                actionNow("$mqttName", "getState")
            }
        }

        route {
            topic = "tele/$mqttName/LWT"
            payload = "Offline"
            handler = {
                logger.info("A $name offline")
            }
        }

        route {
            topic = "stat/$mqttName/RESULT"
            payload = "ON"
            jsonPath = "$.POWER"
            handler = {
                logger.info("A $name bekapcsolt")
            }
        }

        route {
            topic = "stat/$mqttName/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER"
            handler = {
                logger.info("A $name kikapcsolt")
            }
        }

        action {
            id = "getState"
            handler = {
                logger.info("Státusz lekérdezése: $name")
                publish("cmnd/$mqttName/state", "", false)
            }
        }

        action {
            id = "powerOn"
            handler = {
                logger.info("A $name bekapcsolása")
                publish("cmnd/$mqttName/power", "ON", false)
            }
        }

        action {
            id = "powerOff"
            handler = {
                logger.info("A $name kikapcsolása")
                publish("cmnd/$mqttName/power", "OFF", false)
            }
        }

        action {
            id = "toggle"
            handler = {
                logger.info("A $name átkapcsolása")
                publish("cmnd/$mqttName/power", "TOGGLE", false)
            }
        }

    }
}