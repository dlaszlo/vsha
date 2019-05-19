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
@RequestMapping("kapcsoloKonyha")
class KapcsoloKonyha : AbstractDeviceConfig() {

    @Autowired
    lateinit var gpioService: GpioService

    class DeviceState : ResourceSupport() {
        val mqttName: String = "konyha-kapcsolo"
        val name: String = "Konyha lámpakapcsoló ($mqttName)"
        var longPressPowerOn1: Boolean = false
        var longPressPowerOn2: Boolean = false
        var online: Boolean = false
        var powerOn1: Boolean = false
        var powerOn2: Boolean = false
    }

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
                state.powerOn1 = true
                if (!state.longPressPowerOn1) {
                    actionTimeout(KapcsoloKonyha::powerOff1, minutes(30))
                }
            }
        }

        subscribe {
            topic = "stat/${state.mqttName}/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER1"
            handler = {
                logger.info("konyha lámpa kikapcsolt")
                state.powerOn1 = false
                state.longPressPowerOn1 = false
                clearTimeout(KapcsoloKonyha::powerOff1)
            }
        }

        subscribe {
            topic = "stat/${state.mqttName}/RESULT"
            payload = "ON"
            jsonPath = "$.POWER2"
            handler = {
                logger.info("konyhapult lámpa bekapcsolt")
                state.powerOn2 = true
                if (!state.longPressPowerOn2) {
                    actionTimeout(KapcsoloKonyha::powerOff2, minutes(30))
                }
            }
        }

        subscribe {
            topic = "stat/${state.mqttName}/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER2"
            handler = {
                logger.info("konyhapult lámpa kikapcsolt")
                state.powerOn2 = false
                state.longPressPowerOn2 = false
                clearTimeout(KapcsoloKonyha::powerOff2)
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
            topic = "cmnd/konyha-kapcsolo-topic/POWER2"
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
                clearTimeout(KapcsoloKonyha::powerOff1)
                state.longPressPowerOn1 = if (state.powerOn1) {
                    action(KapcsoloKonyha::powerOff1)
                    false
                } else {
                    action(KapcsoloKonyha::powerOn1)
                    gpioService.beep(100)
                    true
                }
            }
        }

        subscribe {
            topic = "cmnd/konyha-kapcsolo-topic/POWER2"
            payload = "HOLD"
            handler = {
                logger.info("hosszú érintéssel a konyhapult lámpa bekapcsolása")
                clearTimeout(KapcsoloKonyha::powerOff2)
                state.longPressPowerOn2 = if (state.powerOn2) {
                    action(KapcsoloKonyha::powerOff2)
                    false
                } else {
                    action(KapcsoloKonyha::powerOn2)
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

    fun powerOn1(): Boolean {
        logger.info("konyha lámpa bekapcsolás")
        publish("cmnd/${state.mqttName}/power1", "ON", false)
        return true
    }

    fun powerOff1(): Boolean {
        logger.info("konyha lámpa kikapcsolás")
        publish("cmnd/${state.mqttName}/power1", "OFF", false)
        return true
    }

    fun powerOn2(): Boolean {
        logger.info("konyhapult lámpa bekapcsolás")
        publish("cmnd/${state.mqttName}/power2", "ON", false)
        return true
    }

    fun powerOff2(): Boolean {
        logger.info("konyhapult lámpa kikapcsolás")
        publish("cmnd/${state.mqttName}/power2", "OFF", false)
        return true
    }

    fun toggle(): Boolean {
        logger.info("átkapcsolás")
        if (state.powerOn1 || state.powerOn2) {
            // mindent lekapcsolunk
            if (state.powerOn1) {
                publish("cmnd/${state.mqttName}/power1", "OFF", false)
            }
            if (state.powerOn2) {
                publish("cmnd/${state.mqttName}/power2", "OFF", false)
            }
        } else {
            // de csak a konyhapultot kapcsoljuk fel
            publish("cmnd/${state.mqttName}/power2", "ON", false)
        }
        return true
    }

    @RequestMapping(produces = arrayOf("application/hal+json"))
    fun getDeviceState(): Resource<DeviceState> {
        val links = arrayListOf<Link>()
        links.add(linkTo(methodOn(this::class.java).getDeviceState()).withSelfRel())
        if (state.online) {
            if (state.powerOn1) {
                links.add(linkTo(methodOn(this::class.java).powerOff1Rest()).withSelfRel())
            } else {
                links.add(linkTo(methodOn(this::class.java).powerOn1Rest()).withSelfRel())
            }
            if (state.powerOn2) {
                links.add(linkTo(methodOn(this::class.java).powerOff2Rest()).withSelfRel())
            } else {
                links.add(linkTo(methodOn(this::class.java).powerOn2Rest()).withSelfRel())
            }

        }
        return Resource(state, links)
    }

    @RequestMapping("/powerOn1")
    fun powerOn1Rest(): ResponseEntity<Boolean> {
        return ResponseEntity(powerOn1(), HttpStatus.OK)
    }

    @RequestMapping("/powerOff1")
    fun powerOff1Rest(): ResponseEntity<Boolean> {
        return ResponseEntity(powerOff1(), HttpStatus.OK)
    }

    @RequestMapping("/powerOn2")
    fun powerOn2Rest(): ResponseEntity<Boolean> {
        return ResponseEntity(powerOn2(), HttpStatus.OK)
    }

    @RequestMapping("/powerOff2")
    fun powerOff2Rest(): ResponseEntity<Boolean> {
        return ResponseEntity(powerOff2(), HttpStatus.OK)
    }

    companion object {
        val logger = LoggerFactory.getLogger(KapcsoloKonyha::class.java)!!
    }


}