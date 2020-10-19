package hu.dlaszlo.vsha.config.devices

import hu.dlaszlo.vsha.backend.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.backend.device.Device
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("erzekeloAjtonyitasKonyha")
class ErzekeloAjtonyitasKonyha : AbstractDeviceConfig() {

    data class DeviceState(
        val mqttName1: String = "konyha-rfbridge",
        val mqttName2: String = "nappali-rfbridge",
        val name: String = "Ajtónyitás érzékelő",
        var doorOpened: Boolean = false
    )

    val state = DeviceState()

    var lastBeepOpen = 0L
    var lastBeepClose = 0L
    var lastMessage = 0L
    var alert = false

    override var device: Device = device {

        subscribe {
            topicList = listOf("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payload = "7B8C0A"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("konyha ajtó kinyitva")
                state.doorOpened = true
                if (currentTime() - lastBeepOpen > seconds(3)) {
                    lastBeepOpen = currentTime()
                    actionTimeout(ErzekeloAjtonyitasKonyha::sendMessage, seconds(45))
                    beeperService.beep(100, 50, 100, 50, 100, 50, 100)
                }
            }
        }

        subscribe {
            topicList = listOf("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payload = "7B8C0E"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("konyha ajtó becsukva")
                state.doorOpened = false
                if (currentTime() - lastBeepClose > seconds(3)) {
                    lastBeepClose = currentTime()
                    clearTimeout(ErzekeloAjtonyitasKonyha::sendMessage)
                    if (alert) {
                        alert = false
                        telegramService.sendNotification("A konyha felőli bejárati ajtó becsukva.")
                    }
                    beeperService.beep(200, 50, 200)
                }
            }
        }

    }

    fun sendMessage(): Boolean {
        if (currentTime() - lastMessage > minutes(1)) {
            logger.info("Üzenet küldése")
            lastMessage = currentTime()
            alert = true
            telegramService.sendNotification("Riasztás! A konyha felőli bejárati ajtó nyitva maradt.")
        }
        return true
    }

    companion object {
        val logger = LoggerFactory.getLogger(ErzekeloAjtonyitasKonyha::class.java)!!
    }
}