package hu.dlaszlo.vsha.config.devices

import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.backend.device.Switch
import hu.dlaszlo.vsha.backend.device.SwitchState
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("kapcsoloFolyoso")
class KapcsoloFolyoso : AbstractDeviceConfig(), Switch {

    data class DeviceState(
        override var displayOrder: Int = 510,
        override var groupName: String = "Folyosó",
        override var mqttName: String = "folyoso-kapcsolo",
        override var name: String = "Folyosó lámpa",
        var lastPowerOff: Long = 0,
        var automaticPowerOff: Boolean = false,
        var forcedPowerOn: Boolean = false,
        var delayedPowerOn: Boolean = false
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
                state.lastPowerOff = 0
                state.automaticPowerOff = false
                action(KapcsoloFolyoso::getState)
            }
        }

        subscribe {
            topic = "tele/${state.mqttName}/LWT"
            payload = "Offline"
            handler = {
                logger.info("offline")
                state.online = false
                state.lastPowerOff = 0
                state.automaticPowerOff = false
            }
        }

        subscribe {
            topic = "stat/${state.mqttName}/RESULT"
            payload = "ON"
            jsonPath = "$.POWER"
            handler = {
                logger.info("bekapcsolt")
                state.delayedPowerOn = true
                state.powerOn = true
                state.lastPowerOff = 0
                state.automaticPowerOff = false
                if (!state.forcedPowerOn) {
                    actionTimeout(KapcsoloFolyoso::automaticPowerOff, seconds(30))
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
                state.forcedPowerOn = false
                state.lastPowerOff = when {
                    state.automaticPowerOff -> 0
                    else -> currentTime()
                }
                state.automaticPowerOff = false
                clearTimeout(KapcsoloFolyoso::automaticPowerOff)
            }
        }

        subscribe {
            topic = "cmnd/folyoso-kapcsolo-topic/POWER"
            payload = "TOGGLE"
            handler = {
                logger.info("dupla érintéssel a konyha kapcsolók kapcsolása")
                action(KapcsoloKonyhapult::toggle)
                action(KapcsoloKonyha::powerOff)
            }
        }

        subscribe {
            topic = "cmnd/folyoso-kapcsolo-topic/POWER"
            payload = "HOLD"
            handler = {
                logger.info("hosszú érintéssel a folyosó lámpa bekapcsolása")
                clearTimeout(KapcsoloFolyoso::powerOff)
                state.forcedPowerOn = true
                action(KapcsoloFolyoso::powerOn)
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

    fun automaticPowerOn(): Boolean {
        val time = currentTime()
        return if (!state.delayedPowerOn || time - minutes(1) > state.lastPowerOff) {
            logger.info("bekapcsolás")
            publish("cmnd/${state.mqttName}/power", "ON", false)
            true
        } else {
            logger.info(
                "nem kapcsolható be jelenleg, hátralévő idő: {} s",
                (minutes(1) + state.lastPowerOff - time) / 1000
            )
            false
        }
    }

    fun automaticPowerOff(): Boolean {
        logger.info("kikapcsolás")
        publish("cmnd/${state.mqttName}/power", "OFF", false)
        state.automaticPowerOff = true
        return true
    }

    override fun toggle(): Boolean {
        KonnektorNappali.logger.info("átkapcsolás")
        publish("cmnd/${state.mqttName}/power", "TOGGLE", false)
        return true
    }

    companion object {
        val logger = LoggerFactory.getLogger(KapcsoloFolyoso::class.java)!!
    }

}
