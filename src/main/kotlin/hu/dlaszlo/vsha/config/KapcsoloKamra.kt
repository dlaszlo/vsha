package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.device.GpioService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.Link
import org.springframework.hateoas.Resource
import org.springframework.hateoas.ResourceSupport
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("kapcsoloKamra")
class KapcsoloKamra : AbstractDeviceConfig() {

    @Autowired
    lateinit var gpioService: GpioService

    class DeviceState : ResourceSupport() {
        val mqttName: String = "kamra-kapcsolo"
        val name: String = "Kamra lámpakapcsoló ($mqttName)"
        var online: Boolean = false
        var powerOn: Boolean = false
    }

    var state = DeviceState()

    override var device = device {

        subscribe {
            topic = "tele/${state.mqttName}/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                state.online = true
                action(KapcsoloKamra::getState)
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
                actionTimeout(KapcsoloKamra::powerOff, minutes(2))
            }
        }

        subscribe {
            topic = "stat/${state.mqttName}/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER"
            handler = {
                logger.info("kikapcsolt")
                state.powerOn = false
                clearTimeout(KapcsoloKamra::powerOff)
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

    @RequestMapping(produces = arrayOf("application/hal+json"))
    fun getDeviceState(): Resource<DeviceState> {
        val links = arrayListOf<Link>()
        links.add(linkTo(methodOn(this::class.java).getDeviceState()).withSelfRel())
        if (state.online) {
            if (state.powerOn) {
                links.add(linkTo(methodOn(this::class.java).powerOffRest()).withSelfRel())
            } else {
                links.add(linkTo(methodOn(this::class.java).powerOnRest()).withSelfRel())
            }
        }
        return Resource(state, links)
    }

    @RequestMapping("/powerOn")
    fun powerOnRest(): ResponseEntity<Boolean> {
        return ResponseEntity(powerOn(), HttpStatus.OK)
    }

    @RequestMapping("/powerOff")
    fun powerOffRest(): ResponseEntity<Boolean> {
        return ResponseEntity(powerOff(), HttpStatus.OK)
    }

    companion object {
        val logger = LoggerFactory.getLogger(KapcsoloKamra::class.java)!!
    }


}