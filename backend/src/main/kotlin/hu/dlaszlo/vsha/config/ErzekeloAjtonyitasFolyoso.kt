package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import hu.dlaszlo.vsha.device.BeeperService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.Arrays.asList

@Component("erzekeloAjtonyitasFolyoso")
class ErzekeloAjtonyitasFolyoso : AbstractDeviceConfig() {

    @Autowired
    lateinit var beeperService: BeeperService

    data class DeviceState(
        val mqttName1: String = "konyha-rfbridge",
        val mqttName2: String = "nappali-rfbridge",
        val name: String = "Folyosó ajtónyitás érzékelő ($mqttName1, $mqttName2)",
        var doorOpened: Boolean = false
    )

    val state = DeviceState()

    var lastBeepOpen = 0L
    var lastBeepClose = 0L


    override var device: Device = device {

        subscribe {
            topicList = asList("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payload = "D12E0A"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("folyosó ajtó kinyitva")
                state.doorOpened = true
                if (currentTime() - lastBeepOpen > seconds(3)) {
                    lastBeepOpen = currentTime()
                    beeperService.beep(100, 50, 100, 50, 100, 50, 100)
                }
            }
        }

        subscribe {
            topicList = asList("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payload = "D12E0E"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("folyosó ajtó becsukva")
                state.doorOpened = false
                if (currentTime() - lastBeepClose > seconds(3)) {
                    lastBeepClose = currentTime()
                    beeperService.beep(200, 50, 200)
                }
            }
        }

    }

    companion object {
        val logger = LoggerFactory.getLogger(ErzekeloAjtonyitasFolyoso::class.java)!!
    }
}