package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.Device
import org.slf4j.LoggerFactory
import org.springframework.hateoas.Resource
import org.springframework.hateoas.ResourceSupport
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("rfBridgeKonyha")
class RfBridgeKonyha : AbstractDeviceConfig() {

    class DeviceState : ResourceSupport() {
        val mqttName: String = "konyha-rfbridge"
        val name: String = "Konyha RF-bridge ($mqttName)"
        var online: Boolean = false
    }

    var state = DeviceState()

    override var device: Device = device {

        subscribe {
            topic = "tele/${state.mqttName}/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                state.online = true
                action(RfBridgeKonyha::getState)
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
    }

    fun getState(): Boolean {
        logger.info("státusz lekérdezése")
        publish("cmnd/${state.mqttName}/state", "", false)
        return true
    }

    @RequestMapping(produces = arrayOf("application/hal+json"))
    fun getDeviceState(): Resource<DeviceState> {
        val link1 = linkTo(methodOn(this::class.java).getDeviceState()).withSelfRel()
        return Resource(state, link1)
    }

    companion object {
        val logger = LoggerFactory.getLogger(RfBridgeKonyha::class.java)!!
    }
}