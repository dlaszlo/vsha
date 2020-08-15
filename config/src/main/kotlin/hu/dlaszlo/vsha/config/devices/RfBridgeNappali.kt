package hu.dlaszlo.vsha.config.devices

import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.backend.device.Device
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("rfBridgeNappali")
class RfBridgeNappali : AbstractDeviceConfig() {

    data class DeviceState(
        val mqttName: String = "nappali-rfbridge",
        val name: String = "Nappali RF-bridge",
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
                action(RfBridgeNappali::getState)
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
        val logger = LoggerFactory.getLogger(RfBridgeNappali::class.java)!!
    }
}