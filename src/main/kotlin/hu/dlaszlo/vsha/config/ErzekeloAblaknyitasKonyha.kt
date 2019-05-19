package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import org.slf4j.LoggerFactory
import org.springframework.hateoas.Resource
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Arrays.asList

@RestController
@RequestMapping("erzekeloAblaknyitasKonyha")
class ErzekeloAblaknyitasKonyha : AbstractDeviceConfig() {

    data class DeviceState(
        val mqttName1: String = "konyha-rfbridge",
        val mqttName2: String = "nappali-rfbridge",
        val name: String = "Konyha ablaknyitás érzékelő ($mqttName1, $mqttName2)",
        var windowOpened: Boolean = false
    )

    val state = DeviceState()

    override var device: Device = device {

        subscribe {
            topicList = asList("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payload = "E1860A"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("ablak kinyitva")
                state.windowOpened = true
                action(VentilatorKonyha::powerOn)
                actionTimeout(VentilatorKonyha::powerOff, minutes(5))
            }
        }

        subscribe {
            topicList = asList("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payload = "E1860E"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("ablak becsukva")
                state.windowOpened = false
                action(VentilatorKonyha::powerOff)
            }
        }

    }

    @RequestMapping(produces = arrayOf("application/hal+json"))
    fun getDeviceState(): Resource<DeviceState> {
        val link1 = linkTo(methodOn(this::class.java).getDeviceState()).withSelfRel()
        return Resource(state, link1)
    }

    companion object {
        val logger = LoggerFactory.getLogger(ErzekeloAblaknyitasKonyha::class.java)!!
    }
}