package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.GpioService
import hu.dlaszlo.vsha.device.Switch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component("kapcsoloTerasz")
class KapcsoloTerasz : AbstractDeviceConfig(), Switch {

    @Autowired
    lateinit var gpioService: GpioService

    data class DeviceState(
        val mqttName: String = "eloszoba-kapcsolo",
        val name: String = "Terasz lámpakapcsoló ($mqttName)",
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
                action(KapcsoloTerasz::getState)
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
            jsonPath = "$.POWER1"
            handler = {
                logger.info("bekapcsolt")
                state.powerOn = true
                actionTimeout(KapcsoloTerasz::powerOff, minutes(15))
            }
        }

        subscribe {
            topic = "stat/${state.mqttName}/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER1"
            handler = {
                logger.info("kikapcsolt")
                state.powerOn = false
                clearTimeout(KapcsoloTerasz::powerOff)
            }
        }
    }

    fun getState(): Boolean {
        logger.info("státusz lekérdezése")
        publish("cmnd/${state.mqttName}/state", "", false)
        return true
    }

    override fun powerOn(): Boolean {
        logger.info("bekapcsolás")
        publish("cmnd/${state.mqttName}/power1", "ON", false)
        return true
    }

    override fun powerOff(): Boolean {
        logger.info("kikapcsolás")
        publish("cmnd/${state.mqttName}/power1", "OFF", false)
        return true
    }

    override fun toggle(): Boolean {
        logger.info("átkapcsolás")
        publish("cmnd/${state.mqttName}/power1", "TOGGLE", false)
        return true
    }

    companion object {
        val logger = LoggerFactory.getLogger(KapcsoloTerasz::class.java)!!
    }

}
