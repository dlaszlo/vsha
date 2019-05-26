package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.GpioService
import hu.dlaszlo.vsha.device.Switch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component("kapcsoloKonyha1")
class KapcsoloKonyha : AbstractDeviceConfig(), Switch {

    @Autowired
    lateinit var gpioService: GpioService

    data class DeviceState(
        val mqttName: String = "konyha-kapcsolo",
        val name: String = "Konyha lámpakapcsoló ($mqttName)",
        var longPressPowerOn: Boolean = false,
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
                action(KapcsoloKonyha::getState)
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
                logger.info("konyha lámpa bekapcsolt")
                state.powerOn = true
                if (!state.longPressPowerOn) {
                    actionTimeout(KapcsoloKonyha::powerOff, minutes(30))
                }
            }
        }

        subscribe {
            topic = "stat/${state.mqttName}/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER1"
            handler = {
                logger.info("konyha lámpa kikapcsolt")
                state.powerOn = false
                state.longPressPowerOn = false
                clearTimeout(KapcsoloKonyha::powerOff)
            }
        }

        subscribe {
            topic = "cmnd/konyha-kapcsolo-topic/POWER1"
            payload = "TOGGLE"
            handler = {
                KapcsoloNappali.logger.info("dupla érintéssel a folyosó lámpa kapcsolása")
                action(KapcsoloFolyoso::toggle)
            }
        }

        subscribe {
            topic = "cmnd/konyha-kapcsolo-topic/POWER1"
            payload = "HOLD"
            handler = {
                logger.info("hosszú érintéssel a konyha lámpa bekapcsolása")
                clearTimeout(KapcsoloKonyha::powerOff)
                state.longPressPowerOn = true
                action(KapcsoloKonyha::powerOn)
                gpioService.beep(100)
            }
        }

    }

    fun getState(): Boolean {
        logger.info("státusz lekérdezése")
        publish("cmnd/${state.mqttName}/state", "", false)
        return true
    }

    override fun powerOn(): Boolean {
        logger.info("konyha lámpa bekapcsolás")
        publish("cmnd/${state.mqttName}/power1", "ON", false)
        return true
    }

    override fun powerOff(): Boolean {
        logger.info("konyha lámpa kikapcsolás")
        publish("cmnd/${state.mqttName}/power1", "OFF", false)
        return true
    }

    override fun toggle(): Boolean {
        KonnektorNappali.logger.info("átkapcsolás")
        publish("cmnd/${state.mqttName}/power", "TOGGLE", false)
        return true
    }

    companion object {
        val logger = LoggerFactory.getLogger(KapcsoloKonyha::class.java)!!
    }


}