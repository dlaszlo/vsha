package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.springframework.stereotype.Component

@Component("kapcsolo2")
open class Kapcsolo2 : AbstractDeviceConfig() {

    override var device = device {

        mqttName = "kapcsolo2"
        name = "Folyosó lámpakapcsoló ($mqttName)"

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