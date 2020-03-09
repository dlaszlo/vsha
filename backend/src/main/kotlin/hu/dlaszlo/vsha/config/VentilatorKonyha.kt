package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Switch
import hu.dlaszlo.vsha.device.SwitchState
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("ventilatorKonyha")
class VentilatorKonyha : AbstractDeviceConfig(), Switch {

    data class DeviceState(
        var mqttName: String = "konyha-ventilator",
        var scheduledTurnedOn: Boolean = false,
        override var name: String = "Konyha ventilator ($mqttName)"
    ) : SwitchState()

    var state = DeviceState()

    override var switchState: SwitchState = state

    var lastTurnOff = 0L

    override var device = device {

        initialize {
            actionCron(VentilatorKonyha::scheduledPowerOn, "0 0 * * * *")
            actionCron(VentilatorKonyha::scheduledPowerOff, "0 5 * * * *")
        }

        subscribe {
            topic = "tele/${state.mqttName}/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                action(VentilatorKonyha::getState)
                state.online = true
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
            }
        }

        subscribe {
            topic = "stat/${state.mqttName}/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER"
            handler = {
                logger.info("kikapcsolt")
                state.powerOn = false
            }
        }
    }

    override fun getState(): Boolean {
        logger.info("státusz lekérdezése")
        publish("cmnd/${state.mqttName}/state", "", false)
        return true
    }

    override fun powerOn(): Boolean {
        if (!state.scheduledTurnedOn && currentTime() - lastTurnOff > minutes(1)) {
            logger.info("bekapcsolás")
            publish("cmnd/${state.mqttName}/power", "ON", false)
            return true
        }
        return false
    }

    override fun powerOff(): Boolean {
        if (!state.scheduledTurnedOn) {
            logger.info("kikapcsolás")
            lastTurnOff = currentTime()
            publish("cmnd/${state.mqttName}/power", "OFF", true)
            return true
        }
        return false
    }

    override fun toggle(): Boolean {
        logger.info("átkapcsolás")
        publish("cmnd/${state.mqttName}/power", "TOGGLE", false)
        return true
    }

    fun scheduledPowerOn(): Boolean {
        if (currentTime() - lastTurnOff > minutes(1)) {
            state.scheduledTurnedOn = true
            logger.info("időzített bekapcsolás")
            publish("cmnd/${state.mqttName}/power", "ON", false)
        }
        return true
    }

    fun scheduledPowerOff(): Boolean {
        logger.info("időzített kikapcsolás")
        lastTurnOff = currentTime()
        state.scheduledTurnedOn = false
        publish("cmnd/${state.mqttName}/power", "OFF", true)
        return true
    }

    companion object {
        val logger = LoggerFactory.getLogger(VentilatorKonyha::class.java)!!
    }

}
