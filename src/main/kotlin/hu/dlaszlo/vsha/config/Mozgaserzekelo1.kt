package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.springframework.stereotype.Component
import java.time.LocalTime

@Component("mozgaserzekelo1")
open class Mozgaserzekelo1 : AbstractDeviceConfig() {

    override var device = device {

        mqttName = "rfbridge1"
        name = "Folyosó mozgásérzékelő ($mqttName)"

        route {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                actionNow("mozgaserzekelo1", "getState")
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
            topic = "tele/$mqttName/RESULT"
            payload = "EC27FE"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("mozgás észlelve")
                val hour = LocalTime.now().hour
                if (hour >= 20 || hour < 6) {
                    actionNow("kapcsolo2", "powerOn")
                    actionTimeout("kapcsolo2", "powerOff", 30 * 1000)
                }
            }
        }

        action {
            id = "getState"
            handler = {
                logger.info("státusz lekérdezése")
                publish("cmnd/$mqttName/state", "", false)
            }
        }

    }
}