package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.BeeperService
import hu.dlaszlo.vsha.device.Switch
import hu.dlaszlo.vsha.device.SwitchState
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component("kapcsoloFurdoszobaTukor")
class KapcsoloFurdoszobaTukor : AbstractDeviceConfig(), Switch {

    @Autowired
    lateinit var beeperService: BeeperService

    data class DeviceState(
        val mqttName: String = "furdoszoba-tukor-kapcsolo",
        override var name: String = "Fürdőszoba tükör lámpakapcsoló ($mqttName)",
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
                action(KapcsoloFurdoszobaTukor::getState)
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
                    actionTimeout(KapcsoloFurdoszobaTukor::powerOff, minutes(60))
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
                clearTimeout(KapcsoloFurdoszobaTukor::powerOff)
            }
        }

        subscribe {
            topic = "cmnd/furdo-tukor-kapcsolo-topic/POWER"
            payload = "TOGGLE"
            handler = {
                logger.info("dupla érintéssel a konyha kapcsolók kapcsolása")
                action(KapcsoloKonyhapult::toggle)
                action(KapcsoloKonyha::powerOff)
            }
        }


        subscribe {
            topic = "cmnd/furdo-tukor-kapcsolo-topic/POWER"
            payload = "HOLD"
            handler = {
                logger.info("hosszú érintéssel a fürdőszoba lámpa bekapcsolása")
                clearTimeout(KapcsoloFurdoszobaTukor::powerOff)
                state.longPressPowerOn = true
                action(KapcsoloFurdoszobaTukor::powerOn)
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
        val logger = LoggerFactory.getLogger(KapcsoloFurdoszobaTukor::class.java)!!
    }

}
