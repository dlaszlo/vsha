package hu.dlaszlo.vsha.config.devices

import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.backend.device.Switch
import hu.dlaszlo.vsha.backend.device.SwitchState
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("kapcsoloNappali")
class KapcsoloNappali : AbstractDeviceConfig(), Switch {

    data class DeviceState(
        override var displayOrder: Int = 100,
        override var groupName: String = "Nappali",
        override var mqttName: String = "nappali-kapcsolo",
        override var name: String = "Lámpa",
        var longPressPowerOn: Boolean = false
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
                action(KapcsoloNappali::getState)
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
            jsonPath = "$.POWER"
            handler = {
                logger.info("bekapcsolt")
                state.powerOn = true
                if (!state.longPressPowerOn) {
                    actionTimeout(KapcsoloNappali::powerOff, minutes(30))
                }
            }
        }

        subscribe {
            topic = "stat/${state.mqttName}/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER"
            handler = {
                logger.info("kikapcsolt")
                state.powerOn = false
                state.longPressPowerOn = false
                clearTimeout(KapcsoloNappali::powerOff)
            }
        }

        subscribe {
            topic = "cmnd/nappali-kapcsolo-topic/POWER"
            payload = "TOGGLE"
            handler = {
                logger.info("dupla érintéssel a nappali állólámpa kapcsolása")
                action(KonnektorNappali::toggle)
            }
        }

        subscribe {
            topic = "cmnd/nappali-kapcsolo-topic/POWER"
            payload = "HOLD"
            handler = {
                logger.info("hosszú érintéssel a nappali lámpa bekapcsolása")
                clearTimeout(KapcsoloNappali::powerOff)
                state.longPressPowerOn = true
                action(KapcsoloNappali::powerOn)
                beeperService.beep(100)
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
        publish("cmnd/${state.mqttName}/power", "ON", false)
        return true
    }

    override fun powerOff(): Boolean {
        logger.info("kikapcsolás")
        publish("cmnd/${state.mqttName}/power", "OFF", false)
        return true
    }

    override fun toggle(): Boolean {
        logger.info("átkapcsolás")
        publish("cmnd/${state.mqttName}/power", "TOGGLE", false)
        return true
    }

    companion object {
        val logger = LoggerFactory.getLogger(KapcsoloNappali::class.java)!!
    }

}
