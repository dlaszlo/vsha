package hu.dlaszlo.vsha.config.devices

import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.backend.device.Device
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("erzekeloTaviranyito")
class ErzekeloTaviranyito : AbstractDeviceConfig() {

    data class DeviceState(
        val mqttName1: String = "konyha-rfbridge",
        val mqttName2: String = "nappali-rfbridge",
        val name: String = "Távírányító"
    )

    var state = DeviceState()

    var lastToggle1 = 0L
    var lastToggle2 = 0L

    override var device: Device = device {

        subscribe {
            topicList = listOf("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payloadList = listOf("195941", "888881", "94AB61")
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("Lámpák kikapcsolása")
                action(KapcsoloFolyoso::powerOff)
                action(KapcsoloFurdoszoba::powerOff)
                action(KapcsoloFurdoszobaTukor::powerOff)
                action(KapcsoloKamra::powerOff)
                action(KapcsoloKonyha::powerOff)
                action(KapcsoloKonyhapult::powerOff)
                action(KapcsoloNappali::powerOff)
                action(KonnektorNappali::powerOff)
                action(KapcsoloGyerekszoba::powerOff)
                action(KonnektorFoldgomb::powerOff)
                action(KonnektorIroasztalLampa::powerOff)
                action(KapcsoloHaloszoba::powerOff)
                action(KapcsoloEloszoba::powerOff)
                action(KapcsoloTerasz::powerOff)
                action(KapcsoloUdvar::powerOff)
            }
        }

        subscribe {
            topicList = listOf("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payloadList = listOf("195942", "888882", "94AB62")
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("Nappali állólámpa bekapcsolása")
                action(KonnektorIroasztalLampa::powerOn)
                action(KapcsoloFurdoszobaTukor::powerOn)
                action(KapcsoloKonyhapult::powerOn)
            }
        }

        subscribe {
            topicList = listOf("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payloadList = listOf("195944", "888884", "94AB64")
            jsonPath = "$.RfReceived.Data"
            handler = {
                if (currentTime() - lastToggle1 >= seconds(1)) {
                    logger.info("Nappali állólámpa átkapcsolása")
                    lastToggle1 = currentTime()
                    action(KonnektorNappali::toggle)
                }
            }
        }

        subscribe {
            topicList = listOf("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payloadList = listOf("195948", "888888", "94AB68")
            jsonPath = "$.RfReceived.Data"
            handler = {
                if (currentTime() - lastToggle2 >= seconds(1)) {
                    logger.info("Földgömb átkapcsolása")
                    lastToggle2 = currentTime()
                    action(KonnektorFoldgomb::toggle)
                }
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(ErzekeloTaviranyito::class.java)!!
    }


}
