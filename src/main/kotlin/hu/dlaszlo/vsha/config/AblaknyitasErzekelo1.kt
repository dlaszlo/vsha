package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import org.springframework.stereotype.Component

@Component("ablaknyitaserzekelo1")
open class AblaknyitasErzekelo1 : AbstractDeviceConfig() {

    override var device: Device = device {

        mqttName = "rfbridge2"
        name = "Konyha ablaknyitás érzékelő ($mqttName)"

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                actionNow("ablaknyitaserzekelo1", "getState")
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
            payload = "E1860A"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("ablak kinyitva")
                actionNow("ventilator1", "powerOn")
                actionTimeout("ventilator1", "powerOff", minutes(5))
            }
        }

        subscribe {
            topic = "tele/$mqttName/RESULT"
            payload = "E1860E"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("ablak becsukva")
                actionNow("ventilator1", "powerOff")
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