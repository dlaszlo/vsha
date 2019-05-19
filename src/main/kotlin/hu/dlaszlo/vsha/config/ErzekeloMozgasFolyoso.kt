package hu.dlaszlo.vsha.config

import hu.dlaszlo.vsha.device.AbstractDeviceConfig
import hu.dlaszlo.vsha.sunsetsunrise.SunsetSunriseService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.Resource
import org.springframework.hateoas.ResourceSupport
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Arrays.asList

@RestController
@RequestMapping("erzekeloMozgasFolyoso")
class ErzekeloMozgasFolyoso : AbstractDeviceConfig() {

    @Autowired
    lateinit var sunsetSunriseService: SunsetSunriseService

    class DeviceState : ResourceSupport() {
        val mqttName1: String = "konyha-rfbridge"
        val mqttName2: String = "nappali-rfbridge"
        val name: String = "Folyosó mozgásérzékelő ($mqttName1, $mqttName2)"
    }

    val state = DeviceState()

    override var device = device {

        subscribe {
            topicList = asList("tele/${state.mqttName1}/RESULT", "tele/${state.mqttName2}/RESULT")
            payload = "EC27FE"
            jsonPath = "$.RfReceived.Data"
            handler = {
                logger.info("mozgás észlelve")
                if (!sunsetSunriseService.isDaylight()) {
                    action(KapcsoloFolyoso::powerOn)
                }
            }
        }
    }

    @RequestMapping(produces = arrayOf("application/hal+json"))
    fun getDeviceState(): Resource<DeviceState> {
        val link1 = linkTo(methodOn(this::class.java).getDeviceState()).withSelfRel()
        return Resource(state, link1)
    }

    companion object {
        val logger = LoggerFactory.getLogger(ErzekeloMozgasFolyoso::class.java)!!
    }
}