package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalTime

@Component("mozgaserzekelo1")
open class Mozgaserzekelo1 : AbstractDeviceConfig() {

    val mqttName = "rfbridge1"
    val name = "Folyosó mozgásérzékelő ($mqttName)"

    override var device = device {

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                actionNow<Mozgaserzekelo1>("getState") { device ->
                    device.getState()
                }
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
                    actionNow<Kapcsolo2>("powerOn") { device ->
                        device.powerOn()
                    }
                    actionTimeout<Kapcsolo2>("powerOff", seconds(30)) { device ->
                        device.powerOff()
                    }
                }
            }
        }
    }

    fun getState() {
        logger.info("státusz lekérdezése")
        publish("cmnd/$mqttName/state", "", false)
    }

    companion object {
        val logger = LoggerFactory.getLogger(Mozgaserzekelo1::class.java)!!
    }


}