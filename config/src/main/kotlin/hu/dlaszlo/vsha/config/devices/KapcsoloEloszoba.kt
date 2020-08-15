package hu.dlaszlo.vsha.config.devices

import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.backend.device.Switch
import hu.dlaszlo.vsha.backend.device.SwitchState
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("kapcsoloEloszoba")
class KapcsoloEloszoba : AbstractDeviceConfig(), Switch {

    data class DeviceState(
        override var displayOrder: Int = 500,
        override var groupName: String = "Folyosó",
        override var mqttName: String = "eloszoba-kapcsolo",
        override var name: String = "Előszoba lámpa"
    ) : SwitchState()

    var state = DeviceState()

    override var switchState: SwitchState = state

    override var device = device {

        subscribe {
            topic = "tele/${state.mqttName}/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                state.online = true
                action(KapcsoloEloszoba::getState)
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

        subscribe {
            topic = "stat/${state.mqttName}/RESULT"
            payload = "ON"
            jsonPath = "$.POWER2"
            handler = {
                logger.info("bekapcsolt")
                state.powerOn = true
                actionTimeout(KapcsoloEloszoba::powerOff, minutes(2))
            }
        }

        subscribe {
            topic = "stat/${state.mqttName}/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER2"
            handler = {
                logger.info("kikapcsolt")
                state.powerOn = false
                clearTimeout(KapcsoloEloszoba::powerOff)
            }
        }
    }

    override fun getState(): Boolean {
        logger.info("státusz lekérdezése")
        publish("cmnd/${state.mqttName}/state", "", false)
        return true
    }

    override fun powerOn(): Boolean {
        logger.info("bekapcsolás")
        publish("cmnd/${state.mqttName}/power2", "ON", false)
        return true
    }

    override fun powerOff(): Boolean {
        logger.info("kikapcsolás")
        publish("cmnd/${state.mqttName}/power2", "OFF", false)
        return true
    }

    override fun toggle(): Boolean {
        logger.info("átkapcsolás")
        publish("cmnd/${state.mqttName}/power2", "TOGGLE", false)
        return true
    }

    companion object {
        val logger = LoggerFactory.getLogger(KapcsoloEloszoba::class.java)!!
    }

}
