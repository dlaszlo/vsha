package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("ventilator1")
open class Ventilator1 : AbstractDeviceConfig() {

    val mqttName = "ventilator1"
    val name = "Konyha ventilator ($mqttName)"
    var turnOnDevice: String? = null
    var lastTurnOff = 0L

    override var device = device {

        initialize {

            actionCron<Ventilator1>("powerOn", "0 0 * * * *") { device ->
                device.powerOn("ventilator1")
            }

            actionCron<Ventilator1>("powerOff", "0 5 * * * *") { device ->
                device.powerOff("ventilator1")
            }

        }

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                actionNow<Ventilator1>("getStatus") { device ->
                    device.getState()
                }
            }
        }

        subscribe {
            topic = "tele/$mqttName/LWT"
            payload = "Offline"
            handler = {
                logger.info("offline")
            }
        }

        subscribe {
            topic = "stat/$mqttName/RESULT"
            payload = "ON"
            jsonPath = "$.POWER"
            handler = {
                logger.info("bekapcsolt")
            }
        }

        subscribe {
            topic = "stat/$mqttName/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER"
            handler = {
                logger.info("kikapcsolt")
            }
        }
    }

    fun getState() {
        logger.info("státusz lekérdezése")
        publish("cmnd/$mqttName/state", "", false)
    }

    fun powerOn(callerDeviceId: String) {
        if (currentTime() - lastTurnOff > minutes(5)
            && (turnOnDevice == null || callerDeviceId == "ventilator1")
        ) {
            logger.info("bekapcsolás")
            turnOnDevice = callerDeviceId
            publish("cmnd/$mqttName/power", "ON", false)
        }
    }

    fun powerOff(callerDeviceId: String) {
        if (turnOnDevice == callerDeviceId) {
            logger.info("kikapcsolás")
            turnOnDevice = null
            lastTurnOff = currentTime()
            publish("cmnd/$mqttName/power", "OFF", false)
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(Ventilator1::class.java)!!
    }

}
