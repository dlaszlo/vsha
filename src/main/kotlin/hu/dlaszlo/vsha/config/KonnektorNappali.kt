package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.slf4j.LoggerFactory

open class KonnektorNappali : AbstractDeviceConfig() {

    data class DeviceState(
        val mqttName: String = "nappali-konnektor",
        val name: String = "Nappali konnektor ($mqttName)",
        var online: Boolean = false,
        var powerOn: Boolean = false
    )

    var state = DeviceState()

    override var device = device {

        subscribe {
            topic = "tele/${state.mqttName}/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                state.online = true
                action(KonnektorNappali::getState)
            }
        }

        subscribe {
            topic = "tele/${state.mqttName}/LWT"
            payload = "Offline"
            handler = {
                state.online = false
                logger.info("offline")
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

    fun getState(): Boolean {
        logger.info("státusz lekérdezése")
        publish("cmnd/${state.mqttName}/state", "", false)
        return true
    }

    fun powerOn(): Boolean {
        logger.info("bekapcsolás")
        publish("cmnd/${state.mqttName}/power", "ON", false)
        return true
    }

    fun powerOff(): Boolean {
        logger.info("kikapcsolás")
        publish("cmnd/${state.mqttName}/power", "OFF", false)
        return true
    }

    fun toggle(): Boolean {
        logger.info("átkapcsolás")
        publish("cmnd/${state.mqttName}/power", "TOGGLE", false)
        return true
    }

    companion object {
        val logger = LoggerFactory.getLogger(KonnektorNappali::class.java)!!
    }

}