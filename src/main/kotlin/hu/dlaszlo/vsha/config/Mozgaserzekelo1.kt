package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.springframework.stereotype.Component
import java.time.LocalTime

@Component("mozgaserzekelo1")
open class Mozgaserzekelo1 : AbstractDeviceConfig() {

    override var device = device {

        mqttName = "rfbridge1"
        name = "Folyosó mozgásérzékelő ($mqttName)"

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                actionNow("mozgaserzekelo1", "getState")
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
            topic = "tele/$mqttName/RESULT"
            payload = "EC27FE"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("mozgás észlelve")
                val hour = LocalTime.now().hour
                if (hour >= 20 || hour < 6) {
                    actionNow("kapcsolo2", "powerOn")
                    actionTimeout("kapcsolo2", "powerOff", seconds(30))
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