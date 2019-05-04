package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("ablaknyitaserzekelo1")
open class AblaknyitasErzekelo1 : AbstractDeviceConfig() {

    val mqttName = "rfbridge2"
    val name = "Konyha ablaknyitás érzékelő ($mqttName)"

    override var device: Device = device {

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                actionNow<AblaknyitasErzekelo1>("getState") { device ->
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
            payload = "E1860A"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("ablak kinyitva")

                actionNow<Ventilator1>("powerOn") { device ->
                    device.powerOn("ablaknyitaserzekelo1")
                }

                actionTimeout<Ventilator1>("powerOff", minutes(5)) { device ->
                    device.powerOff("ablaknyitaserzekelo1")
                }

            }
        }

        subscribe {
            topic = "tele/$mqttName/RESULT"
            payload = "E1860E"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("ablak becsukva")
                actionNow<Ventilator1>("powerOff") { device ->
                    device.powerOff("ablaknyitaserzekelo1")
                }
            }
        }

    }

    fun getState() {
        logger.info("státusz lekérdezése")
        publish("cmnd/$mqttName/state", "", false)
    }

    companion object {
        val logger = LoggerFactory.getLogger(AblaknyitasErzekelo1::class.java)!!
    }
}