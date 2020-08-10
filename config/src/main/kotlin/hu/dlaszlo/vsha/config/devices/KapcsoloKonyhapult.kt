package hu.dlaszlo.vsha.config.devices

import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.backend.device.Switch
import hu.dlaszlo.vsha.backend.device.SwitchState
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("kapcsoloKonyhapult")
class KapcsoloKonyhapult : AbstractDeviceConfig(), Switch {

    data class DeviceState(
            val mqttName: String = "konyha-kapcsolo",
            override var name: String = "Konyhapult lámpakapcsoló",
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
                action(KapcsoloKonyhapult::getState)
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
            jsonPath = "$.POWER2"
            handler = {
                logger.info("konyhapult lámpa bekapcsolt")
                state.delayedPowerOn = true
                state.powerOn = true
                state.lastPowerOff = 0
                state.automaticPowerOff = false
                if (!state.forcedPowerOn) {
                    actionTimeout(KapcsoloKonyhapult::automaticPowerOff, minutes(60))
                }
            }
        }

        subscribe {
            topic = "stat/${state.mqttName}/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER2"
            handler = {
                logger.info("konyhapult lámpa kikapcsolt")
                state.powerOn = false
                state.forcedPowerOn = false
                state.lastPowerOff = when {
                    state.automaticPowerOff -> 0
                    else -> currentTime()
                }
                state.automaticPowerOff = false
                clearTimeout(KapcsoloKonyhapult::automaticPowerOff)
            }
        }

        subscribe {
            topic = "cmnd/konyha-kapcsolo-topic/POWER2"
            payload = "TOGGLE"
            handler = {
                KapcsoloNappali.logger.info("dupla érintéssel a folyosó lámpa kapcsolása")
                action(KapcsoloFolyoso::toggle)
            }
        }

        subscribe {
            topic = "cmnd/konyha-kapcsolo-topic/POWER2"
            payload = "HOLD"
            handler = {
                logger.info("hosszú érintéssel a konyhapult lámpa bekapcsolása")
                clearTimeout(KapcsoloKonyhapult::powerOff)
                state.forcedPowerOn = true
                action(KapcsoloKonyhapult::powerOn)
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
        logger.info("konyhapult lámpa bekapcsolás")
        publish("cmnd/${state.mqttName}/power2", "ON", false)
        return true
    }

    override fun powerOff(): Boolean {
        logger.info("konyhapult lámpa kikapcsolás")
        publish("cmnd/${state.mqttName}/power2", "OFF", false)
        return true
    }

    fun automaticPowerOn(): Boolean {
        val time = currentTime()
        return if (!state.delayedPowerOn || time - minutes(1) > state.lastPowerOff) {
            logger.info("bekapcsolás")
            publish("cmnd/${state.mqttName}/power2", "ON", false)
            true
        } else {
            logger.info("nem kapcsolható be jelenleg, hátralévő idő: {} s",
                    (minutes(1) + state.lastPowerOff - time) / 1000)
            false
        }
    }

    fun automaticPowerOff(): Boolean {
        logger.info("kikapcsolás")
        publish("cmnd/${state.mqttName}/power2", "OFF", false)
        state.automaticPowerOff = true
        return true
    }

    override fun toggle(): Boolean {
        logger.info("átkapcsolás")
        val kapcsoloKonyha: KapcsoloKonyha = getDevice()
        if (kapcsoloKonyha.state.powerOn || state.powerOn) {
            if (state.powerOn) {
                publish("cmnd/${state.mqttName}/power2", "OFF", false)
            }
        } else {
            publish("cmnd/${state.mqttName}/power2", "ON", false)
        }
        return true
    }

    companion object {
        val logger = LoggerFactory.getLogger(KapcsoloKonyhapult::class.java)!!
    }


}