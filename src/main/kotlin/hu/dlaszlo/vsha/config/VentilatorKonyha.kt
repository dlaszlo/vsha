package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import org.slf4j.LoggerFactory
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
@RequestMapping("ventilatorKonyha")
class VentilatorKonyha : AbstractDeviceConfig() {

    class DeviceState : ResourceSupport() {
        val mqttName: String = "konyha-ventilator"
        val name: String = "Konyha ventilator ($mqttName)"
        var scheduledTurnedOn: Boolean = false
        var online: Boolean = false
        var powerOn: Boolean = false
    }

    var state = DeviceState()

    var lastTurnOff = 0L

    override var device = device {

        initialize {
            actionCron(VentilatorKonyha::scheduledPowerOn, "0 0 * * * *")
            actionCron(VentilatorKonyha::scheduledPowerOff, "0 5 * * * *")
        }

        subscribe {
            topic = "tele/${state.mqttName}/LWT"
            payload = "Online"
            handler = {
                logger.info("online")
                action(VentilatorKonyha::getState)
                state.online = true
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
            }
        }

        subscribe {
            topic = "stat/${state.mqttName}/RESULT"
            payload = "OFF"
            jsonPath = "$.POWER"
            handler = {
                logger.info("kikapcsolt")
                state.powerOn = false
            }
        }
    }

    fun getState(): Boolean {
        logger.info("státusz lekérdezése")
        publish("cmnd/${state.mqttName}/state", "", false)
        return true
    }

    fun powerOn(): Boolean {
        if (!state.scheduledTurnedOn && currentTime() - lastTurnOff > minutes(1)) {
            logger.info("bekapcsolás")
            publish("cmnd/${state.mqttName}/power", "ON", false)
            return true
        }
        return false
    }

    fun powerOff(): Boolean {
        if (!state.scheduledTurnedOn) {
            logger.info("kikapcsolás")
            lastTurnOff = currentTime()
            publish("cmnd/${state.mqttName}/power", "OFF", true)
            return true
        }
        return false
    }

    fun scheduledPowerOn(): Boolean {
        if (currentTime() - lastTurnOff > minutes(1)) {
            state.scheduledTurnedOn = true
            logger.info("időzített bekapcsolás")
            publish("cmnd/${state.mqttName}/power", "ON", false)
        }
        return true
    }

    fun scheduledPowerOff(): Boolean {
        logger.info("időzített kikapcsolás")
        lastTurnOff = currentTime()
        state.scheduledTurnedOn = false
        publish("cmnd/${state.mqttName}/power", "OFF", true)
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
        val logger = LoggerFactory.getLogger(VentilatorKonyha::class.java)!!
    }

}
