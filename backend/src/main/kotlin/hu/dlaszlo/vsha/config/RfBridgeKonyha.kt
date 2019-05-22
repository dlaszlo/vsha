package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("rfBridgeKonyha")
class RfBridgeKonyha : AbstractDeviceConfig() {

    data class DeviceState(
        val mqttName: String = "konyha-rfbridge",
        val name: String = "Konyha RF-bridge ($mqttName)",
        var online: Boolean = false
    )

    var state = DeviceState()

    override var device: Device = device {

        subscribe {
            topic = "tele/${state.mqttName}/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                state.online = true
                action(RfBridgeKonyha::getState)
            }
        }

        subscribe {
            topic = "tele/${state.mqttName}/LWT"
            payload = "Offline"
            handler = {
                logger.info("offline")
                state.online = false
            }
        }
    }

    fun getState(): Boolean {
        logger.info("státusz lekérdezése")
        publish("cmnd/${state.mqttName}/state", "", false)
        return true
    }

    companion object {
        val logger = LoggerFactory.getLogger(RfBridgeKonyha::class.java)!!
    }
}