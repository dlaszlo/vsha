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
@RequestMapping("kapcsoloFolyoso")
class KapcsoloFolyoso : AbstractDeviceConfig() {

    @Autowired
    lateinit var gpioService: GpioService

    class DeviceState : ResourceSupport() {
        val mqttName: String = "folyoso-kapcsolo"
        val name: String = "Folyosó lámpakapcsoló ($mqttName)"
        var longPressPowerOn: Boolean = false
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
                action(KapcsoloFolyoso::getState)
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
                    actionTimeout(KapcsoloFolyoso::powerOff, seconds(30))
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
                clearTimeout(KapcsoloFolyoso::powerOff)
            }
        }

        subscribe {
            topic = "cmnd/folyoso-kapcsolo-topic/POWER"
            payload = "TOGGLE"
            handler = {
                logger.info("dupla érintéssel a konyha kapcsolók kapcsolása")
                action(KapcsoloKonyha::toggle)
            }
        }

        subscribe {
            topic = "cmnd/folyoso-kapcsolo-topic/POWER"
            payload = "HOLD"
            handler = {
                logger.info("hosszú érintéssel a folyosó lámpa bekapcsolása")
                clearTimeout(KapcsoloFolyoso::powerOff)
                state.longPressPowerOn = if (state.powerOn) {
                    action(KapcsoloFolyoso::powerOff)
                    false
                } else {
                    action(KapcsoloFolyoso::powerOn)
                    gpioService.beep(100)
                    true
                }
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
        KonnektorNappali.logger.info("átkapcsolás")
        publish("cmnd/${state.mqttName}/power", "TOGGLE", false)
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
        val logger = LoggerFactory.getLogger(KapcsoloFolyoso::class.java)!!
    }

}
